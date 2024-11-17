package top.zedo

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * 请求数据类，用于传递 TTS（文本到语音）合成请求。
 */
@Serializable
data class TTSRequest(
    /**
     * 要合成的文本
     * 必填字段，表示需要合成的文本内容。
     */
    val text: String,

    /**
     * 要合成文本的语言
     * 必填字段，指定要合成文本的语言类型。
     */
    val text_lang: String,

    /**
     * 参考音频的路径
     * 必填字段，指定用于合成的参考音频的路径。
     */
    val ref_audio_path: String,

    /**
     * 用于多说话人音色融合的辅助参考音频路径
     * 可选字段，用于提供额外的参考音频来增强合成效果。
     */
    val aux_ref_audio_paths: List<String> = emptyList(),

    /**
     * 参考音频的提示文本
     * 可选字段，用于提供参考音频的提示文本。
     */
    val prompt_text: String = "",

    /**
     * 参考音频的提示文本语言
     * 必填字段，指定参考音频的提示文本的语言。
     */
    val prompt_lang: String = "",

    /**
     * top-k 采样
     * 用于设置 Top-k 采样策略，默认为 5。
     */
    val top_k: Int = 5,

    /**
     * top-p 采样
     * 用于设置 Top-p 采样策略，默认为 1.0。
     */
    val top_p: Double = 1.0,

    /**
     * 采样温度
     * 控制合成时的采样温度，默认为 1.0。
     */
    val temperature: Double = 1.0,

    /**
     * 文本分割方法
     * 指定合成时的文本分割方法，默认为 "cut0"。
     */
    val text_split_method: String = "cut0",

    /**
     * 推理的批量大小
     * 指定批量推理的大小，默认为 1。
     */
    val batch_size: Int = 1,

    /**
     * 批量拆分的阈值
     * 控制批量拆分的阈值，默认为 0.75。
     */
    val batch_threshold: Double = 0.75,

    /**
     * 是否将批量拆分为多个桶
     * 如果为 true，将批量拆分为多个桶进行处理，默认为 true。
     */
    val split_bucket: Boolean = true,

    /**
     * 控制合成音频的速度
     * 默认为 1.0，值越大音频播放越快。
     */
    val speed_factor: Double = 1.0,

    /**
     * 是否返回流式响应
     * 如果为 true，将返回流式响应，默认为 false。
     */
    val streaming_mode: Boolean = false,

    /**
     * 随机种子，用于复现结果
     * 默认为 -1，可以设置一个具体的整数值以固定生成结果。
     */
    val seed: Int = -1,
    /**
     * 音频类型，支持 “wav”、“raw”、“ogg”、“aac”。
     */
    var media_type: String = "wav",

    /**
     * 是否使用并行推理
     * 如果为 true，将使用并行推理，默认为 true。
     */
    val parallel_infer: Boolean = true,

    /**
     * T2S 模型的重复惩罚系数
     * 控制模型在生成过程中对重复词语的惩罚，默认为 1.35。
     */
    val repetition_penalty: Double = 1.35

)

class ApiException(message: String) : Exception(message)

/**
 * GPT-SoVITS的API
 * https://github.com/RVC-Boss/GPT-SoVITS.git
 */
class GptSoVitsApi : AutoCloseable {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json) }
        expectSuccess = false // 默认不抛出异常，手动处理响应状态码
    }
    private var baseUrl = "http://localhost:9880"

    private suspend fun <T> safeApiCall(call: suspend () -> T): T {
        try {
            return call()
        } catch (e: Exception) {
            throw ApiException("API 请求失败: ${e.localizedMessage}")
        }
    }

    /**
     * 设置GptWeights权重文件
     */
    suspend fun setGptWeights(weightsPath: String) {
        val url = "$baseUrl/set_gpt_weights?weights_path=$weightsPath"
        safeApiCall {
            val response = client.get(url)
            checkResponse(response)
        }
    }

    private suspend fun checkResponse(response: HttpResponse) {
        if (response.status != HttpStatusCode.OK) {
            val errorMessage = response.body<JsonObject>().get("message")?.toString() ?: "未知错误"
            throw ApiException("请求失败: $errorMessage")
        }
    }

    /**
     * 进行TTS推理
     */
    suspend fun tts(requestData: TTSRequest): ByteArray {
        val url = "$baseUrl/tts"
        return safeApiCall {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            checkResponse(response)
            response.body<ByteArray>()
        }
    }

    /**
     * 设置Sovits权重文件
     */
    suspend fun setSovitsWeights(weightsPath: String) {
        val url = "$baseUrl/set_sovits_weights?weights_path=$weightsPath"
        safeApiCall {
            val response = client.get(url)
            checkResponse(response)
        }
    }

    override fun close() {
        client.close()
    }

}

