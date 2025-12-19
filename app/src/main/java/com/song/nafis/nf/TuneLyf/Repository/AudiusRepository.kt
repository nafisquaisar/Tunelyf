    package com.song.nafis.nf.TuneLyf.Repository

    import com.song.nafis.nf.TuneLyf.Api.AudiusApi
    import com.song.nafis.nf.TuneLyf.ApiModel.AudiusTrack
    import com.song.nafis.nf.TuneLyf.Cache.PreloadConfig
    import com.song.nafis.nf.TuneLyf.Cache.TrackRoomCache
    import com.song.nafis.nf.TuneLyf.Model.UnifiedMusic
    import com.song.nafis.nf.TuneLyf.Model.toUnifiedMusic
    import timber.log.Timber
    import java.io.IOException
    import java.net.SocketTimeoutException
    import javax.inject.Inject

    class AudiusRepository @Inject constructor(
        private val api: AudiusApi,
        private val trackRoomCache: TrackRoomCache
    ) {

        private val inFlight = mutableSetOf<String>()

        private val TRENDING_KEY = "TRENDING"

        private val NEW_UPLOADS_KEY = "NEW_UPLOADS"



        /* ---------------- SEARCH = METADATA ONLY ---------------- */

        suspend fun search(
            query: String,
            limit: Int
        ): List<UnifiedMusic> {

            val cached = trackRoomCache.getTracksForQuery(query)
            if (cached.isNotEmpty()) return cached

            val response = api.searchTracks(
                artist = query,
                limit = limit.coerceIn(1, 20),
                offset = 0
            )

            if (!response.isSuccessful) return emptyList()

            val unified = response.body()?.data
                ?.filterNot { it.title.isNullOrBlank() }
                ?.map { it.toUnifiedMusic() }
                .orEmpty()

            trackRoomCache.saveTracks(query, unified)
            return unified
        }

        /* ---------------- GET CURRENT STREAM URL ---------------- */

        suspend fun getStreamUrl(track: UnifiedMusic): String? {

            // ✅ already cached → direct play
            if (!track.musicPath.isNullOrBlank()) {
                Timber.d("📦 STREAM URL FROM DB → ${track.musicId}")
                return track.musicPath
            }

            // ✅ only block parallel calls
            if (!inFlight.add(track.musicId)) {
                Timber.d("⏳ Stream fetch already running for ${track.musicId}")
                return null
            }

            return try {
                Timber.d("🌐 API CALL → stream for ${track.musicId}")
                val response = api.getStreamRedirect(track.musicId)

                if (response.isSuccessful) {
                    val url = response.body()?.streamUrl
                    if (!url.isNullOrBlank()) {
                        trackRoomCache.updateStreamUrl(track.musicId, url)
                        Timber.d("✅ Stream URL saved → ${track.musicId}")
                    }
                    url
                } else {
                    Timber.e("❌ Stream API failed → ${track.musicId}")
                    null
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ Stream fetch error → ${track.musicId}")
                null
            } finally {
                inFlight.remove(track.musicId)
            }
        }


        /* ---------------- PREFETCH NEXT SONG ONLY ---------------- */

        suspend fun prefetchNext(
            query: String,
            index: Int,
            list: List<UnifiedMusic>
        ) {
            val next = list.getOrNull(index + 1) ?: return
            if (!next.musicPath.isNullOrBlank()) return
            if (inFlight.contains(next.musicId)) return

            inFlight.add(next.musicId)

            try {
                val response = api.getStreamRedirect(next.musicId)
                if (response.isSuccessful) {
                    response.body()?.streamUrl?.let { url ->
                        trackRoomCache.updateSingleTrack(
                            query,
                            next.copy(musicPath = url)
                        )
                    }
                }
            } finally {
                inFlight.remove(next.musicId)
            }
        }

        /* ---------------- PAGINATION (METADATA ONLY) ---------------- */

        suspend fun loadNextPage(
            query: String,
            pageSize: Int
        ): List<UnifiedMusic> {

            val cached = trackRoomCache.getTracksForQuery(query)

            val response = api.searchTracks(
                artist = query,
                limit = pageSize,
                offset = cached.size
            )

            if (!response.isSuccessful) return emptyList()

            val newUnified = response.body()?.data
                ?.map { it.toUnifiedMusic() }
                ?.filterNot { u -> cached.any { it.musicId == u.musicId } }
                .orEmpty()

            if (newUnified.isNotEmpty()) {
                trackRoomCache.saveTracks(query, cached + newUnified)
            }

            return newUnified
        }


        suspend fun preloadHomeSections() {

            for (section in PreloadConfig.sections) {

                val cached = trackRoomCache.getTracksForQuery(section)
                if (cached.size >= PreloadConfig.SONGS_PER_SECTION) {
                    Timber.d("✅ Preload skip (already cached): $section")
                    continue
                }

                Timber.d("⬇️ Preloading: $section")

                val response = api.searchTracks(
                    artist = section,
                    limit = PreloadConfig.SONGS_PER_SECTION,
                    offset = 0
                )

                if (!response.isSuccessful) continue

                val tracks = response.body()?.data
                    ?.map { it.toUnifiedMusic() }
                    .orEmpty()

                trackRoomCache.saveTracks(section, tracks)
            }
        }


        suspend fun loadTrending(limit: Int = 20): List<UnifiedMusic> {

            // 1️⃣ DB first
            val cached = trackRoomCache.getTracksForQuery(TRENDING_KEY)
            if (cached.isNotEmpty()) {
                Timber.d("📦 TRENDING FROM CACHE")
                return cached
            }

            // 2️⃣ API call
            Timber.d("🌐 TRENDING API CALL")
            val response = api.getTrendingTracks(limit)

            if (!response.isSuccessful) return emptyList()

            val tracks = response.body()?.data
                ?.map { it.toUnifiedMusic() }
                .orEmpty()

            if (tracks.isNotEmpty()) {
                trackRoomCache.saveTracks(TRENDING_KEY, tracks)
            }

            return tracks
        }




        suspend fun loadNewUploads(limit: Int = 20): List<UnifiedMusic> {

            // 1️⃣ DB first
            val cached = trackRoomCache.getTracksForQuery(NEW_UPLOADS_KEY)
            if (cached.isNotEmpty()) {
                Timber.d("📦 NEW UPLOADS FROM CACHE")
                return cached
            }

            // 2️⃣ API
            Timber.d("🌐 API CALL → NEW UPLOADS")
            val response = api.getNewUploads(limit)

            if (!response.isSuccessful) return emptyList()

            val tracks = response.body()?.data
                ?.map { it.toUnifiedMusic() }
                .orEmpty()

            if (tracks.isNotEmpty()) {
                trackRoomCache.saveTracks(NEW_UPLOADS_KEY, tracks)
            }

            return tracks
        }


    }

