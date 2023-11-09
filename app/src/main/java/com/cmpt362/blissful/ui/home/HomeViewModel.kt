package com.cmpt362.blissful.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cmpt362.blissful.R

class HomeViewModel : ViewModel() {

    private val _gratitudeItems = MutableLiveData<List<GratitudeItem>>().apply {
        value = getSampleGratitudeItems()
    }
    val gratitudeItems: LiveData<List<GratitudeItem>> = _gratitudeItems

    private fun getSampleGratitudeItems(): List<GratitudeItem> {
        return listOf(
            GratitudeItem(
                R.drawable.todays_gratitude,
                "Today's Gratitude",
                "Capture and cherish the moments you are grateful for. Share your gratitude with the wold and let positivity shine"
            ),
            GratitudeItem(
                R.drawable.natures_blessing,
                "Nature's Blessing",
                "Discover the beauty of Caradhras, a majestic iceberg that stands tall as a symbol of gratitude and appreciation"
            )
        )
    }
}