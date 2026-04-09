package com.castlefrog.shuffle.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.castlefrog.shuffle.analytics.AnalyticsLogger
import com.castlefrog.shuffle.analytics.AnalyticsValue
import com.castlefrog.shuffle.analytics.logButtonTap
import com.castlefrog.shuffle.analytics.logViewHidden
import com.castlefrog.shuffle.analytics.logViewVisible
import com.castlefrog.shuffle.model.ShuffleItem
import com.castlefrog.shuffle.model.ShuffleList
import com.castlefrog.shuffle.repository.ShuffleListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val shuffleListRepository: ShuffleListRepository,
) : ViewModel() {
    data class Model(
        var allListNames: MutableList<String> = mutableListOf(),
        var selectedList: ShuffleList? = null,
        var selectedItems: SnapshotStateList<ShuffleItem> = mutableStateListOf(),
    )

    private val model: Model = Model()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiAction = MutableStateFlow<UiAction>(UiAction.None)
    val uiAction: StateFlow<UiAction> = _uiAction

    private fun sendAction(uiAction: UiAction) {
        _uiAction.value = uiAction
        _uiAction.value = UiAction.None
    }

    sealed class UiAction {
        data object None : UiAction()
        data class ShareList(val list: ShuffleList) : UiAction()
    }

    data class UiState(
        val mainView: MainView = MainView.Loading,
        val overlayView: OverlayView? = null,
    ) {
        sealed class MainView {
            data object Empty : MainView()
            data object Loading : MainView()
            data class ShuffleView(
                val shuffleListName: String,
                val numberOfSubsetItems: Int,
                val selectedItems: SnapshotStateList<ShuffleItem>,
            ) : MainView()
            data class EditListView(
                val selectedList: ShuffleList
            ) : MainView()
        }
        sealed class OverlayView {
            data object AddItemView : OverlayView()
            data object AddListView : OverlayView()
            data object ConfirmDeleteItemView : OverlayView()
        }
    }

    sealed class UiEvent {
        data class Init(val initParams: InitParams) : UiEvent() {
            data class InitParams(val selectedListName: String?)
        }

        data object SelectShuffle : UiEvent()
        data object OpenListNames : UiEvent()
        data object OpenEditList : UiEvent()

        data class ChangeList(val name: String) : UiEvent()
        data class CreateNewList(val name: String) : UiEvent()

        data object SelectAddItem : UiEvent()
        data object DismissBottomSheet : UiEvent()
        data class AddItem(val item: ShuffleItem) : UiEvent()
        data object DeleteItem : UiEvent()
        data object ShareList: UiEvent()
        data object ConfirmDelete : UiEvent()
    }

    fun handleUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.Init -> init(uiEvent.initParams)
            UiEvent.DismissBottomSheet -> dismissBottomSheet()
            UiEvent.SelectAddItem -> selectAddItem()
            is UiEvent.AddItem -> addItem(uiEvent.item)
            UiEvent.DeleteItem -> deleteItem()
            UiEvent.ConfirmDelete -> confirmDelete()
            UiEvent.ShareList -> shareList()
            is UiEvent.ChangeList -> TODO()
            is UiEvent.CreateNewList -> TODO()
            UiEvent.OpenEditList -> TODO()
            UiEvent.OpenListNames -> TODO()
            UiEvent.SelectShuffle -> TODO()
        }
    }

    private fun init(initParams: UiEvent.Init.InitParams) {
        load(initParams.selectedListName)
    }

    private fun load(selectedListName: String?) {
        analyticsLogger.logViewVisible(AnalyticsValue.ViewName.MAIN)
        _uiState.update { UiState(mainView = UiState.MainView.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedListName != null) { // name ->
                shuffleListRepository.getShuffleListByName(selectedListName)
                    .catch { Timber.w(it) }
                    .collect {
                        model.selectedList = it
                        updateSelectedItems()
                        _uiState.update {
                            UiState(
                                mainView = UiState.MainView.ShuffleView(
                                    numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                                    shuffleListName = model.selectedList?.name ?: "",
                                    selectedItems = mutableStateListOf<ShuffleItem>().apply { addAll(model.selectedList?.items ?: emptyList()) },
                                )
                            )
                        }
                    }
            } else {
                _uiState.update { UiState(mainView = UiState.MainView.Empty) }
            }
            shuffleListRepository.getAllShuffleListNames()
                .catch { Timber.w(it) }
                .collect { lists ->
                    model.allListNames.clear()
                    model.allListNames.addAll(lists)
                }
        }
    }

    private fun selectAddItem() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.NAVIGATE_TO_ADD)
        _uiState.update {
            it.copy(overlayView = UiState.OverlayView.AddItemView)
        }
    }

    private fun deleteItem() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DELETE_ITEM)
        _uiState.update {
            it.copy(overlayView = UiState.OverlayView.ConfirmDeleteItemView)
        }
    }

    private fun confirmDelete() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CONFIRM_DELETE_ITEM)
        /*selectedItem.onSome {
            viewModelScope.launch(Dispatchers.IO) {
                shuffleListRepository.updateShuffleList(it)
                    .catch { Timber.w(it) }
                    .collect {
                        hideAlert()
                        hideBottomSheet()
                        load()
                    }
            }
        }*/
    }

    private fun addItem(item: ShuffleItem) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.ADD_ITEM)
        viewModelScope.launch(Dispatchers.IO) {
            model.selectedList.let { list ->
                if (list != null) {
                    shuffleListRepository.addItemToShuffleList(list.name, item.text)
                        .catch { Timber.w(it) }
                        .collect {
                            _uiState.update {
                                it.copy()
                            }
                            hideBottomSheet()
                            // load()
                        }
                } else {

                }
            }
        }
    }

    private fun dismissBottomSheet() {
        hideBottomSheet()
    }

    private fun hideBottomSheet() {
        analyticsLogger.logViewHidden(AnalyticsValue.ViewName.BOTTOM_SHEET)
        _uiState.update {
            it.copy(overlayView = null)
        }
    }

    private fun shareList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.SHARE_LIST)
        if (model.selectedList != null) {
            model.selectedList?.let { sendAction(UiAction.ShareList(it)) }
        } else {
            Timber.w("No selected item to share")
        }
    }

    private fun updateSelectedItems() {
        model.selectedItems.clear()
        model.selectedList?.let { list ->
            val randomItems = getRandomSubset(list.items.size, list.subsetSize)
            for (i in 0..randomItems.size) {
                model.selectedItems.add(list.items[i])
            }
        }
    }

    private fun getRandomSubset(listLength: Int, nSelected: Int): List<Int> {
        val effectiveSize = minOf(nSelected, listLength)
        return if (effectiveSize < 1) {
            emptyList()
        } else {
            (0 until listLength).shuffled().take(effectiveSize)
        }
    }
}