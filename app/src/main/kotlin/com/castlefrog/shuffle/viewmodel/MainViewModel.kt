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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val shuffleListRepository: ShuffleListRepository,
) : ViewModel() {
    data class Model(
        var hasInit: Boolean = false,
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
            data object Loading : MainView()
            data class ShuffleView(
                val allListNames: List<String> = emptyList(),
                val selectedListName: String,
                val numberOfSubsetItems: Int,
                val totalItemCount: Int,
                val selectedItems: SnapshotStateList<ShuffleItem>,
            ) : MainView()
        }
        sealed class OverlayView {
            data object AddItemView : OverlayView()
            data object AddListView : OverlayView()
            data object ConfirmDeleteItemView : OverlayView()
            data class ConfirmDeleteListView(val listName: String) : OverlayView()
            data class EditListView(val list: ShuffleList) : OverlayView()
        }
    }

    sealed class UiEvent {
        data object Init : UiEvent()

        data object OpenEditList : UiEvent()

        data class ChangeList(val name: String) : UiEvent()
        data class CreateNewList(val name: String) : UiEvent()

        data object OpenAddList : UiEvent()
        data object Refresh : UiEvent()
        data class SelectItem(val index: Int) : UiEvent()
        data object IncreaseSubsetSize : UiEvent()
        data object DecreaseSubsetSize : UiEvent()
        data object SelectAddItem : UiEvent()
        data object DismissBottomSheet : UiEvent()
        data object ShareList: UiEvent()
        data class RequestDeleteList(val name: String) : UiEvent()
        data object ConfirmDeleteList : UiEvent()
        data class DeleteItemFromList(val itemText: String) : UiEvent()
        data class AddItemToList(val itemText: String) : UiEvent()
    }

    fun handleUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.Init -> init()
            UiEvent.Refresh -> refresh()
            UiEvent.DismissBottomSheet -> dismissBottomSheet()
            UiEvent.SelectAddItem -> selectAddItem()
            UiEvent.ShareList -> shareList()
            is UiEvent.ChangeList -> changeList(uiEvent.name)
            is UiEvent.RequestDeleteList -> requestDeleteList(uiEvent.name)
            UiEvent.ConfirmDeleteList -> confirmDeleteList()
            UiEvent.OpenAddList -> openAddList()
            is UiEvent.CreateNewList -> createNewList(uiEvent.name)
            UiEvent.OpenEditList -> openEditList()
            is UiEvent.DeleteItemFromList -> deleteItemFromList(uiEvent.itemText)
            is UiEvent.AddItemToList -> addItemToList(uiEvent.itemText)
            is UiEvent.SelectItem -> shuffleSelectedItem(uiEvent.index)
            UiEvent.IncreaseSubsetSize -> increaseSubsetSize()
            UiEvent.DecreaseSubsetSize -> decreaseSubsetSize()
        }
    }

    private fun init() {
        if (!model.hasInit) {
            model.hasInit = true
            load()
        }
    }

    private fun refresh() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.REFRESH)
        viewModelScope.launch(Dispatchers.IO) {
            updateSelectedItems()
            _uiState.update { state ->
                val currentView = state.mainView
                if (currentView is UiState.MainView.ShuffleView) {
                    state.copy(
                        mainView = currentView.copy(
                            selectedItems = mutableStateListOf<ShuffleItem>().apply {
                                addAll(model.selectedItems)
                            }
                        )
                    )
                } else state
            }
        }
    }

    private fun load() {
        _uiState.update { UiState(mainView = UiState.MainView.Loading) }
        analyticsLogger.logViewVisible(AnalyticsValue.ViewName.SPLASH)
        viewModelScope.launch(Dispatchers.IO) {
            loadAllShuffleListNames()
            findSelectedList()
            updateSelectedItems()
            _uiState.update {
                UiState(
                    mainView = UiState.MainView.ShuffleView(
                        allListNames = model.allListNames,
                        numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                        totalItemCount = model.selectedList?.items?.size ?: 0,
                        selectedListName = model.selectedList?.name ?: "",
                        selectedItems = mutableStateListOf<ShuffleItem>().apply {
                            addAll(model.selectedItems)
                        },
                    )
                )
            }
            analyticsLogger.logViewVisible(AnalyticsValue.ViewName.MAIN)
        }
    }

    private suspend fun loadAllShuffleListNames() {
        model.allListNames.clear()
        model.allListNames.addAll(
            shuffleListRepository.getAllShuffleListNames()
            .catch { Timber.w(it) }
            .first()
        )
    }

    private suspend fun findSelectedList() {
        model.selectedList = shuffleListRepository.getCurrentSelectedList()
            .catch { Timber.w(it) }
            .firstOrNull()
    }

    private fun selectAddItem() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.NAVIGATE_TO_ADD_ITEM)
        _uiState.update {
            it.copy(overlayView = UiState.OverlayView.AddItemView)
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

    private fun changeList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CHANGE_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.setCurrentSelectedList(name).single()
            findSelectedList()
            updateSelectedItems()
            _uiState.update {
                UiState(
                    mainView = UiState.MainView.ShuffleView(
                        allListNames = model.allListNames,
                        numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                        totalItemCount = model.selectedList?.items?.size ?: 0,
                        selectedListName = model.selectedList?.name ?: "",
                        selectedItems = mutableStateListOf<ShuffleItem>().apply {
                            addAll(model.selectedItems)
                        },
                    )
                )
            }
        }
    }

    private fun openAddList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.NAVIGATE_TO_ADD_LIST)
        _uiState.update { it.copy(overlayView = UiState.OverlayView.AddListView) }
    }

    private fun createNewList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CREATE_NEW_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.createShuffleList(name).catch { Timber.w(it) }.single()
            shuffleListRepository.setCurrentSelectedList(name).single()
            loadAllShuffleListNames()
            findSelectedList()
            updateSelectedItems()
            _uiState.update {
                UiState(
                    mainView = UiState.MainView.ShuffleView(
                        allListNames = model.allListNames,
                        numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                        totalItemCount = model.selectedList?.items?.size ?: 0,
                        selectedListName = model.selectedList?.name ?: "",
                        selectedItems = mutableStateListOf<ShuffleItem>().apply {
                            addAll(model.selectedItems)
                        },
                    )
                )
            }
        }
    }

    private fun openEditList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.OPEN_EDIT_LIST)
        model.selectedList?.let { list ->
            _uiState.update { it.copy(overlayView = UiState.OverlayView.EditListView(list)) }
        }
    }

    private fun deleteItemFromList(itemText: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DELETE_ITEM, mapOf(Pair(AnalyticsValue.ValueName.NAME, itemText)))
        val listName = model.selectedList?.name ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.removeItemFromShuffleList(listName, itemText)
                .catch { Timber.w(it) }.single()
            findSelectedList()
            model.selectedList?.let { list ->
                _uiState.update { it.copy(overlayView = UiState.OverlayView.EditListView(list)) }
            }
        }
    }

    private fun addItemToList(itemText: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.ADD_ITEM, mapOf(Pair(AnalyticsValue.ValueName.NAME, itemText)))
        val listName = model.selectedList?.name ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.addItemToShuffleList(listName, itemText)
                .catch { Timber.w(it) }.single()
            findSelectedList()
            model.selectedList?.let { list ->
                _uiState.update { it.copy(overlayView = UiState.OverlayView.EditListView(list)) }
            }
        }
    }

    private fun requestDeleteList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.REQUEST_DELETE_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        _uiState.update {
            it.copy(overlayView = UiState.OverlayView.ConfirmDeleteListView(name))
        }
    }

    private fun confirmDeleteList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CONFIRM_DELETE_LIST)
        val listName = (_uiState.value.overlayView as? UiState.OverlayView.ConfirmDeleteListView)?.listName ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.deleteShuffleList(listName).catch { Timber.w(it) }.single()
            loadAllShuffleListNames()
            if (model.selectedList?.name == listName) {
                val nextName = model.allListNames.firstOrNull()
                if (nextName != null) {
                    shuffleListRepository.setCurrentSelectedList(nextName).single()
                }
            }
            findSelectedList()
            updateSelectedItems()
            _uiState.update {
                UiState(
                    mainView = UiState.MainView.ShuffleView(
                        allListNames = model.allListNames,
                        numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                        totalItemCount = model.selectedList?.items?.size ?: 0,
                        selectedListName = model.selectedList?.name ?: "",
                        selectedItems = mutableStateListOf<ShuffleItem>().apply {
                            addAll(model.selectedItems)
                        },
                    )
                )
            }
        }
    }

    private fun updateSelectedItems() {
        model.selectedItems.clear()
        model.selectedList?.let { list ->
            val randomIndices = getRandomSubset(list.items.size, list.subsetSize)
            for (index in randomIndices) {
                model.selectedItems.add(list.items[index])
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

    private fun increaseSubsetSize() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.INCREASE_SUBSET_SIZE)
        val list = model.selectedList ?: return
        val currentView = _uiState.value.mainView as? UiState.MainView.ShuffleView ?: return
        if (currentView.selectedItems.size >= list.items.size) return

        val excludedItems = currentView.selectedItems.toSet()
        val available = list.items.filter { it !in excludedItems }
        if (available.isEmpty()) return

        val newItem = available.random()
        val newSubsetSize = currentView.selectedItems.size + 1
        model.selectedList = list.copy(subsetSize = newSubsetSize)
        model.selectedItems.add(newItem)
        currentView.selectedItems.add(newItem)

        model.selectedList?.name?.let { name ->
            viewModelScope.launch(Dispatchers.IO) {
                shuffleListRepository.incListSubsetSize(name)
                    .catch { Timber.w(it) }
                    .single()

                _uiState.update { state ->
                    (state.mainView as? UiState.MainView.ShuffleView)?.let { view ->
                        state.copy(mainView = view.copy(numberOfSubsetItems = newSubsetSize))
                    } ?: state
                }
            }
        }
    }

    private fun decreaseSubsetSize() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DECREASE_SUBSET_SIZE)
        val list = model.selectedList ?: return
        val currentView = _uiState.value.mainView as? UiState.MainView.ShuffleView ?: return
        if (currentView.selectedItems.size <= 1) return

        val newSubsetSize = currentView.selectedItems.size - 1
        model.selectedList = list.copy(subsetSize = newSubsetSize)
        model.selectedItems.removeLastOrNull()
        currentView.selectedItems.removeLastOrNull()

        model.selectedList?.name?.let { name ->
            viewModelScope.launch(Dispatchers.IO) {
                shuffleListRepository.decListSubsetSize(name)
                    .catch { Timber.w(it) }
                    .single()

                _uiState.update { state ->
                    (state.mainView as? UiState.MainView.ShuffleView)?.let { view ->
                        state.copy(mainView = view.copy(numberOfSubsetItems = newSubsetSize))
                    } ?: state
                }
            }
        }
    }

    private fun shuffleSelectedItem(index: Int) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.SELECT_ITEM)
        val list = model.selectedList ?: return
        val currentView = _uiState.value.mainView as? UiState.MainView.ShuffleView ?: return
        if (index < 0 || index >= currentView.selectedItems.size) return

        val excludedItems = currentView.selectedItems.toSet()
        val available = list.items.filter { it !in excludedItems }
        if (available.isEmpty()) return

        val newItem = available.random()
        currentView.selectedItems[index] = newItem
        model.selectedItems[index] = newItem
    }
}