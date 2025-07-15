package com.song.nafis.nf.TuneLyf.Model

import com.song.nafis.nf.TuneLyf.R

object Itemlist {

    val trendingList: List<HomeItemSqModel> = listOf(
        HomeItemSqModel("1", "Chill Beats", "Relaxing instrumentals", R.drawable.chillbeats),
        HomeItemSqModel("2", "Morning Vibes", "Start your day right", R.drawable.morningvibes),
        HomeItemSqModel("3", "Focus Flow", "For deep work sessions", R.drawable.focusflow),
        HomeItemSqModel("4", "Waw Beats", "Relaxing instrumentals", R.drawable.wawbeats),
        HomeItemSqModel("5", "Rap Caveat", "Top trending rap tracks", R.drawable.rapcaveat),
//        HomeItemSqModel("6", "Hot & New 🔥", "Fresh tracks making waves", R.drawable.homeitemcoverimg),
//        HomeItemSqModel("7", "Bangers Only", "High-energy hits", R.drawable.homeitemcoverimg),
//        HomeItemSqModel("8", "Underground Hype", "Emerging underground artists", R.drawable.homeitemcoverimg),
//        HomeItemSqModel("9", "Electronic Bangers ⚡", "Electrifying electronic tunes", R.drawable.homeitemcoverimg),
//        HomeItemSqModel("10", "R&B Vibes", "Smooth and soulful R&B", R.drawable.homeitemcoverimg)
    )


    val recentList: List<HomeItemSqModel> = listOf(
        HomeItemSqModel("5", "Night Grooves", "Perfect for evening chill", R.drawable.homeitemcoverimg),
        HomeItemSqModel("6", "Sunny Mornings", "Upbeat and fresh", R.drawable.homeitemcoverimg),
        HomeItemSqModel("7", "Deep Focus", "Concentration music", R.drawable.homeitemcoverimg),
        HomeItemSqModel("8", "Jazz Vibes", "Smooth jazz tunes", R.drawable.homeitemcoverimg)
    )

    val artistList: List<HomeItemSqModel> = listOf(
        HomeItemSqModel("artist_1", "Arijit", "Soulful Bollywood Singer", R.mipmap.arjit_singh),
        HomeItemSqModel("artist_2", "Taylor Swift", "Pop & Country Icon", R.mipmap.taylor_swift),
        HomeItemSqModel("artist_3", "Honey Singh", "English Pop Superstar", R.drawable.honeysingh),
        HomeItemSqModel("artist_4", "Shreya Ghoshal", "Melodious Indian Vocalist", R.drawable.shreya_ghoshal),
        HomeItemSqModel("artist_5", "Diljit Dosanjh", "Punjabi Sensation", R.drawable.diljit_dosanjh),
    )

}
