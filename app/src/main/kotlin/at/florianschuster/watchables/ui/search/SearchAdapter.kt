/*
 * Copyright 2019 Florian Schuster. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.florianschuster.watchables.ui.search

import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.florianschuster.watchables.R
import at.florianschuster.watchables.all.util.srcBlurConsumer
import at.florianschuster.watchables.all.util.srcConsumer
import at.florianschuster.watchables.model.Search
import at.florianschuster.watchables.model.originalPoster
import at.florianschuster.watchables.model.thumbnailPoster
import at.florianschuster.watchables.service.local.PrefRepo
import com.jakewharton.rxrelay2.PublishRelay
import com.tailoredapps.androidutil.ui.extensions.inflate
import io.reactivex.Observable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_search.*
import kotlinx.android.synthetic.main.item_search_rating.*
import org.koin.core.KoinComponent
import org.koin.core.inject

sealed class SearchAdapterInteraction {
    data class AddItemClick(val item: Search.SearchItem) : SearchAdapterInteraction()
    data class ImageClick(val imageUrl: String?) : SearchAdapterInteraction()
    data class OpenItemClick(val item: Search.SearchItem) : SearchAdapterInteraction()
}

class SearchAdapter : ListAdapter<Search.SearchItem, SearchViewHolder>(searchDiff) {
    private val interactionRelay: PublishRelay<SearchAdapterInteraction> = PublishRelay.create()
    val interaction: Observable<SearchAdapterInteraction> = interactionRelay.hide().share()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder = SearchViewHolder(parent.inflate(R.layout.item_search))
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) = holder.bind(getItem(position), interactionRelay::accept)
}

private val searchDiff = object : DiffUtil.ItemCallback<Search.SearchItem>() {
    override fun areItemsTheSame(oldItem: Search.SearchItem, newItem: Search.SearchItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Search.SearchItem, newItem: Search.SearchItem): Boolean = oldItem == newItem
}

class SearchViewHolder(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer, KoinComponent {
    private val prefRepo: PrefRepo by inject()

    fun bind(item: Search.SearchItem, interaction: (SearchAdapterInteraction) -> Unit) {
        containerView.setOnClickListener { interaction(SearchAdapterInteraction.OpenItemClick(item)) }
        tvTitle.text = item.title

        tvType.setText(if (item.type == Search.SearchItem.Type.movie) R.string.display_name_movie else R.string.display_name_show)

        ivImage.clipToOutline = true
        ivImage.srcConsumer(R.drawable.ic_logo).accept(item.thumbnailPoster)
        ivBackground.srcBlurConsumer(R.drawable.ic_logo).accept(item.thumbnailPoster)

        ivImage.setOnClickListener { interaction(SearchAdapterInteraction.ImageClick(item.originalPoster)) }

        ivAdd.setImageResource(if (item.added) R.drawable.ic_check else R.drawable.ic_add)
        when {
            item.added -> R.color.colorAccent
            else -> android.R.color.white
        }.let { ContextCompat.getColor(containerView.context, it) }.let(ivAdd::setColorFilter)

        if (!item.added) {
            ivAdd.setOnClickListener { interaction(SearchAdapterInteraction.AddItemClick(item)) }
        }

        includeRating.isVisible = prefRepo.watchableRatingsEnabled
        tvRating.text = "${item.rating}"
    }
}
