package com.gbros.tabslite.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbros.tabslite.data.ChordVariationRepository
import com.gbros.tabslite.data.TabFull
import com.gbros.tabslite.data.TabFullRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * The ViewModel used in [PlantDetailFragment].
 */
class TabDetailViewModel(
        val tabRepository: TabFullRepository,
        private val chordVariationRepository: ChordVariationRepository,
        private val tabId: Int
) : ViewModel() {
    var getTabJob = viewModelScope.async { tabRepository.getTab(tabId) }
    var tab: TabFull? = null
    //eventually todo: getUsedChords


}
