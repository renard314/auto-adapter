package com.renard.auto_adapter.sample

import com.renard.auto_adapter.AdapterItem
import com.renard.auto_adapter.Unique

@AdapterItem("NewsArticleAdapter")
class Advertisement(private val id: Long) : Unique {

    var message = "This is an advertisement message"

    override fun getId(): Long = this.id
}
