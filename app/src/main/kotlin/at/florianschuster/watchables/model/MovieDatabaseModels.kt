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

package at.florianschuster.watchables.model

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate

data class Search(val results: List<SearchItem?> = emptyList()) {
    data class SearchItem(
        val id: Int,
        val title: String,
        val type: Type,
        val posterPath: String?,
        val rating: Double?,
        val numberOfRatings: Int?,
        val added: Boolean
    ) {
        enum class Type { movie, tv }
    }
}

data class Movie(
    val id: Int,
    @SerializedName("original_title") val name: String,
    @SerializedName("poster_path") val image: String?,
    @SerializedName("homepage") val website: String?,
    @SerializedName("runtime") val durationInMinutes: Long?,
    @SerializedName("original_language") val language: String,
    @SerializedName("overview") val summary: String?,
    val status: Status,
    val videos: Videos,
    @SerializedName("external_ids") val externalIds: ExternalIds,
    val credits: Credits?,
    @SerializedName("vote_average") val rating: Double?,
    @SerializedName("vote_count") val numberOfRatings: Int?,
    @SerializedName("release_dates") val releaseDates: ReleaseDates?
) {
    enum class Status {
        @SerializedName("Rumored")
        rumored,
        @SerializedName("Planned")
        planned,
        @SerializedName("In Production")
        inProduction,
        @SerializedName("Post Production")
        postProduction,
        @SerializedName("Released")
        released,
        @SerializedName("Canceled")
        canceled
    }

    data class ReleaseDates(val results: List<Result>) {
        data class Result(
            @SerializedName("iso_3166_1")
            val countryIso: String,
            @SerializedName("release_dates")
            val dates: List<Date>
        ) {
            data class Date(
                val certification: String,
                @SerializedName("release_date")
                val date: LocalDate,
                val type: Type
            ) {
                enum class Type {
                    Premiere, TheatricalLimited, Theatrical, Digital, Physical, TV
                }
            }
        }
    }
}

data class Show(
    val id: Int,
    @SerializedName("original_name") val name: String,
    @SerializedName("poster_path") val image: String?,
    @SerializedName("homepage") val website: String,
    @SerializedName("number_of_seasons") val seasons: Int,
    @SerializedName("episode_run_time") val episodeRuntimes: List<Int>,
    @SerializedName("external_ids") val externalIds: ExternalIds,
    @SerializedName("first_air_date") val releaseDate: LocalDate?,
    @SerializedName("overview") val summary: String?,
    val status: Status,
    val videos: Videos,
    @SerializedName("next_episode_to_air") val nextEpisode: Season.Episode?,
    @SerializedName("last_episode_to_air") val lastEpisode: Season.Episode?,
    val credits: Credits?,
    @SerializedName("vote_average") val rating: Double?,
    @SerializedName("vote_count") val numberOfRatings: Int?
) {
    enum class Status {
        @SerializedName("Returning Series")
        returningSeries,
        @SerializedName("Planned")
        planned,
        @SerializedName("In Production")
        inProduction,
        @SerializedName("Ended")
        ended,
        @SerializedName("Canceled")
        canceled,
        @SerializedName("Pilot")
        pilot
    }
}

data class ExternalIds(
    @SerializedName("imdb_id") val imdbId: String?,
    @SerializedName("facebook_id") val facebookId: String?,
    @SerializedName("instagram_id") val instagramId: String?,
    @SerializedName("twitter_id") val twitterId: String?
)

data class Season(
    val id: Int,
    @SerializedName("season_number") val index: Int,
    @SerializedName("poster_path") val image: String?,
    @SerializedName("air_date") val airingDate: LocalDate?,
    @SerializedName("episodes") val episodes: List<Episode>
) {
    data class Episode(
        val id: Int,
        val name: String,
        @SerializedName("season_number") val seasonIndex: Int,
        @SerializedName("episode_number") val episodeIndex: Int,
        @SerializedName("air_date") val airingDate: LocalDate?
    )
}

data class Videos(val results: List<Video>) {
    data class Video(
        val id: String,
        val name: String,
        val key: String,
        val type: Type?,
        val site: String
    ) {
        enum class Type {
            @SerializedName("Trailer")
            trailer,
            @SerializedName("Teaser")
            teaser,
            @SerializedName("Clip")
            clip,
            @SerializedName("Featurette")
            featurette,
            @SerializedName("Opening Credits")
            openingCredits
        }

        val isYoutube: Boolean
            get() = site == "YouTube"
    }
}

data class Credits(val id: Int, val cast: List<Cast>) {
    data class Cast(
        @SerializedName("cast_id")
        val id: Int,
        val name: String,
        val order: Int
    )
}

data class Language(
    @SerializedName("iso_639_1")
    val languageIso: String,
    @SerializedName("english_name")
    val englishName: String,
    val name: String
)
