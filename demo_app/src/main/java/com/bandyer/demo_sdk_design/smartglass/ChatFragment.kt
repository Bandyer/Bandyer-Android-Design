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
                    "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit, amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.",
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
                            data.avatar
                        )
                    )
                )
            }
        }
    }
}