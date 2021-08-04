package com.bandyer.demo_sdk_design.smartglass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bandyer.demo_sdk_design.R
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.chat.ChatItem
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatData
import com.bandyer.sdk_design.new_smartglass.chat.SmartGlassChatFragment
import java.util.*

class ChatFragment : SmartGlassChatFragment() {

    private val activity by lazy { requireActivity() as SmartGlassActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as SmartGlassActivity).showStatusBar()
        activity.hideNotification()

        val view = super.onCreateView(inflater, container, savedInstanceState)

        addItem(
            ChatItem(
                SmartGlassChatData(
                    "Mario",
                    "Mario",
                    "Il testo finto Lorem ipum viene utilizzato dai grafici, dai programmatori e dai tipografi con lo scopo di occupare gli spazi di un sito internet, di un prodotto pubblicitario o di una produzione editoriale il cui testo definitivo non è ancora pronto. Questo espediente serve ad avere un’idea del prodotto finito che di lì a poco verrà stampato o diffuso tramite canali digitali. Per avere un risultato più conforme al risultato finale, i grafici, i designer o i tipografi riportano il testo Lorem ipsum nel rispetto di due aspetti fondamentali, ovvero leggibilità ed esigenza editoriale. La scelta del font e della dimensione dei caratteri con cui viene riportato il testo Lorem ipsum risponde a delle precise esigenze che vanno oltre il puro e semplice riempimento di spazi deputati ad accogliere testi reali e che consentono di avere fra le mani un prodotto pubblicitario/editoriale, sia web sia cartaceo, fedele alla realtà. Il testo finto Lorem ipum viene utilizzato dai grafici, dai programmatori e dai tipografi con lo scopo di occupare gli spazi di un sito internet, di un prodotto pubblicitario o di una produzione editoriale il cui testo definitivo non è ancora pronto. Questo espediente serve ad avere un’idea del prodotto finito che di lì a poco verrà stampato o diffuso tramite canali digitali. Per avere un risultato più conforme al risultato finale, i grafici, i designer o i tipografi riportano il testo Lorem ipsum nel rispetto di due aspetti fondamentali, ovvero leggibilità ed esigenza editoriale. La scelta del font e della dimensione dei caratteri con cui viene riportato il testo Lorem ipsum risponde a delle precise esigenze che vanno oltre il puro e semplice riempimento di spazi deputati ad accogliere testi reali e che consentono di avere fra le mani un prodotto pubblicitario/editoriale, sia web sia cartaceo, fedele alla realtà. dIl testo finto Lorem ipum viene utilizzato dai grafici, dai programmatori e dai tipografi con lo scopo di occupare gli spazi di un sito internet, di un prodotto pubblicitario o di una produzione editoriale il cui testo definitivo non è ancora pronto. Questo espediente serve ad avere un’idea del prodotto finito che di lì a poco verrà stampato o diffuso tramite canali digitali. Per avere un risultato più conforme al risultato finale, i grafici, i designer o i tipografi riportano il testo Lorem ipsum nel rispetto di due aspetti fondamentali, ovvero leggibilità ed esigenza editoriale. La scelta del font e della dimensione dei caratteri con cui viene riportato il testo Lorem ipsum risponde a delle precise esigenze che vanno oltre il puro e semplice riempimento di spazi deputati ad accogliere testi reali e che consentono di avere fra le mani un prodotto pubblicitario/editoriale, sia web sia cartaceo, fedele alla realtà. Il testo finto Lorem ipum viene utilizzato dai grafici, dai programmatori e dai tipografi con lo scopo di occupare gli spazi di un sito internet, di un prodotto pubblicitario o di una produzione editoriale il cui testo definitivo non è ancora pronto. Questo espediente serve ad avere un’idea del prodotto finito che di lì a poco verrà stampato o diffuso tramite canali digitali. Per avere un risultato più conforme al risultato finale, i grafici, i designer o i tipografi riportano il testo Lorem ipsum nel rispetto di due aspetti fondamentali, ovvero leggibilità ed esigenza editoriale. La scelta del font e della dimensione dei caratteri con cui viene riportato il testo Lorem ipsum risponde a delle precise esigenze che vanno oltre il puro e semplice riempimento di spazi deputati ad accogliere testi reali e che consentono di avere fra le mani un prodotto pubblicitario/editoriale, sia web sia cartaceo, fedele alla realtà. ",
                    Date().time,
                    R.drawable.sample_image
                )
            )
        )
        addItem(
            ChatItem(
                SmartGlassChatData(
                    "Ugo",
                    "Ugo",
                    "Come se fosse antani con lo scappellamento a sinistra",
                    Date().time
                )
            )
        )
        addItem(
            ChatItem(
                SmartGlassChatData(
                    "Gianfranco",
                    "Gianfranco",
                    "Mi piacciono i treni",
                    Date().time
                )
            )
        )

        counter!!.text = "+3"
//        Handler(Looper.getMainLooper()).postDelayed({
//            itemAdapter!!.add(ChatItem("Francesco: La scatola è sulla sinistra"))
//
//        }, 200)
        return view
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean =
        when (event) {
            SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
                findNavController().popBackStack()
                true
            }
            else -> false
        }

    private fun addItem(item: ChatItem) {
        val data = item.data
        messageTextView?.post {
            messageTextView!!.text = data.message
            val pageList = messageTextView!!.paginate()
            for (i in pageList.indices) {
                var page = pageList[i]
                page = page.removePrefix(" ")
                page = page.removeSuffix(" ")
                val text = when (i) {
                    0 -> resources.getString(
                        R.string.bandyer_smartglass_three_dots_end_pattern,
                        page.toString()
                    )
                    pageList.size - 1 -> resources.getString(
                        R.string.bandyer_smartglass_three_dots_start_pattern,
                        page.toString()
                    )
                    else -> resources.getString(
                        R.string.bandyer_smartglass_three_dots_pattern,
                        page.toString()
                    )
                }

                itemAdapter!!.add(
                    ChatItem(
                        SmartGlassChatData(
                            data.name,
                            data.userAlias,
                            text,
                            data.time,
                            data.avatar,
                            i == 0
                        )
                    )
                )
            }
        }
    }
}