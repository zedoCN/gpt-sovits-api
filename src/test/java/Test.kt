import top.zedo.GptSoVitsApi
import top.zedo.TTSRequest
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

suspend fun main() {
    val audioFormat = AudioFormat(32000F, 16, 1, true, false)
    val sourceDataLine = AudioSystem.getSourceDataLine(audioFormat)
    sourceDataLine.open()
    sourceDataLine.start()
    GptSoVitsApi().run {
        setGptWeights("GPT_weights_v2/momona-cn-e10.ckpt")
//        setGptWeights("GPT_SoVITS/pretrained_models/s1bert25hz-2kh-longer-epoch=68e-step=50232.ckpt")
        setSovitsWeights("SoVITS_weights_v2/momona-cn_e16_s4160.pth")
//        setSovitsWeights("GPT_SoVITS/pretrained_models/s2G488k.pth")
        val data = tts(
            TTSRequest(
                text = "欸，这就射辣？",
                text_lang = "auto",
                ref_audio_path = "【中立_neutral】雪天也要记得撑伞哦，不然雪化了浑身都湿漉漉的。.wav",
                aux_ref_audio_paths = listOf(
                    "【中立_neutral】今天要做的事情做了吗？.wav",
                    "【中立_neutral】很晚了呦，阁下不睡了吗？.wav",
                    "【中立_neutral】阁下为什么要摸梦梦奈的腿呢？有哪里不对吗？.wav",
                    "【中立_neutral】雪天也要记得撑伞哦，不然雪化了浑身都湿漉漉的。.wav"
                ),
                text_split_method = "cut5",
                prompt_text = "雪天也要记得撑伞哦，不然雪化了浑身都湿漉漉的。",
                prompt_lang = "zh"
            )
        )
        //Files.write(Path.of("a.wav"), data)
        val inp = AudioSystem.getAudioInputStream(data.inputStream())
        while (inp.available() > 0) {
            val buf: ByteArray = inp.readBytes()
            sourceDataLine.write(buf, 0, buf.size)
        }
        close()
    }
}