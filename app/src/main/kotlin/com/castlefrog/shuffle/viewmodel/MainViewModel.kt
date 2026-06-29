package com.castlefrog.shuffle.viewmodel

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

class MainViewModel(
    private val analyticsLogger: AnalyticsLogger,
    private val shuffleListRepository: ShuffleListRepository,
) : ViewModel() {
    data class Model(
        var hasInit: Boolean = false,
        var allListNames: MutableList<String> = mutableListOf(),
        var selectedList: ShuffleList? = null,
        var selectedItems: MutableList<ShuffleItem> = mutableListOf(),
    )

    private val model: Model = Model()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
                val selectedItems: List<ShuffleItem>,
            ) : MainView()
        }
        sealed class OverlayView {
            data object AddListView : OverlayView()
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
        data object DismissBottomSheet : UiEvent()
        data object ShareList: UiEvent()
        data class RequestDeleteList(val name: String) : UiEvent()
        data class ConfirmDeleteList(val name: String) : UiEvent()
        data class DeleteItemFromList(val text: String) : UiEvent()
        data class AddItemToList(val text: String) : UiEvent()
    }

    fun handleUiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.Init -> init()
            UiEvent.Refresh -> refresh()
            UiEvent.DismissBottomSheet -> dismissBottomSheet()
            UiEvent.ShareList -> shareList()
            is UiEvent.ChangeList -> changeList(uiEvent.name)
            is UiEvent.RequestDeleteList -> requestDeleteList(uiEvent.name)
            is UiEvent.ConfirmDeleteList -> confirmDeleteList(uiEvent.name)
            UiEvent.OpenAddList -> openAddList()
            is UiEvent.CreateNewList -> createNewList(uiEvent.name)
            UiEvent.OpenEditList -> openEditList()
            is UiEvent.DeleteItemFromList -> deleteItemFromList(uiEvent.text)
            is UiEvent.AddItemToList -> addItemToList(uiEvent.text)
            is UiEvent.SelectItem -> shuffleSelectedItem(uiEvent.index)
            UiEvent.IncreaseSubsetSize -> increaseSubsetSize()
            UiEvent.DecreaseSubsetSize -> decreaseSubsetSize()
        }
    }

    private fun init() {
        if (!model.hasInit) {
            model.hasInit = true
            updateUi(UiState(mainView = UiState.MainView.Loading))
            analyticsLogger.logViewVisible(AnalyticsValue.ViewName.SPLASH)
            viewModelScope.launch(Dispatchers.IO) {
                loadAllShuffleListNames()
                findSelectedList()
                updateSelectedItems()
                updateUi()
                analyticsLogger.logViewVisible(AnalyticsValue.ViewName.MAIN)
            }
        }
    }

    private fun refresh() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.REFRESH)
        viewModelScope.launch(Dispatchers.IO) {
            updateSelectedItems()
            updateUi()
        }
    }

    private fun generateMainUiState(): UiState {
        return UiState(
            mainView = UiState.MainView.ShuffleView(
                allListNames = model.allListNames,
                numberOfSubsetItems = model.selectedList?.subsetSize ?: 1,
                totalItemCount = model.selectedList?.items?.size ?: 0,
                selectedListName = model.selectedList?.name ?: "",
                selectedItems = model.selectedItems.toList(),
            )
        )
    }

    private fun updateUi(
        uiState: UiState = generateMainUiState()
    ) {
        _uiState.update { uiState }
    }

    private suspend fun loadAllShuffleListNames() {
        model.allListNames.clear()
        model.allListNames.addAll(
            shuffleListRepository.getAllShuffleListNames()
            .catch { Timber.w(it) }
            .first()
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun findSelectedList() {
        model.selectedList = shuffleListRepository.getCurrentSelectedListIndex()
            .map { if (model.allListNames.size > it) model.allListNames[it] else null }
            .flatMapMerge {
                if (it != null) shuffleListRepository.getShuffleListByName(it) else flowOf(null)
            }
            .catch { Timber.w(it) }
            .firstOrNull()
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
        TODO()
    }

    private fun changeList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CHANGE_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.setCurrentSelectedListIndex(model.allListNames.indexOf(name)).single()
            findSelectedList()
            updateSelectedItems()
            updateUi()
        }
    }

    private fun openAddList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.NAVIGATE_TO_ADD_LIST)
        _uiState.update { it.copy(overlayView = UiState.OverlayView.AddListView) }
    }

    private fun createNewList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CREATE_NEW_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        viewModelScope.launch(Dispatchers.IO) {
            createList(name)
            shuffleListRepository.setCurrentSelectedListIndex(model.allListNames.size - 1).single()
            findSelectedList()
            updateSelectedItems()
            updateUi()
        }
    }

    private suspend fun createList(name: String) {
        model.allListNames.addLast(name)
        shuffleListRepository.createShuffleList(name).catch { Timber.w(it) }.single()
    }

    private fun openEditList() {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.OPEN_EDIT_LIST)
        model.selectedList?.let { list ->
            _uiState.update { it.copy(overlayView = UiState.OverlayView.EditListView(list)) }
        }
    }

    private fun deleteItemFromList(itemText: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DELETE_ITEM, mapOf(Pair(AnalyticsValue.ValueName.NAME, itemText)))
        val selectedList = model.selectedList ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.removeItemFromShuffleList(selectedList.name, itemText)
                .catch { Timber.w(it) }.single()
            val newSelectedList = selectedList.copy(items = selectedList.items - ShuffleItem(itemText))
            model.selectedList = newSelectedList
            updateSelectedItems()
            val uiState = generateMainUiState()
                .copy(overlayView = UiState.OverlayView.EditListView(newSelectedList))
            updateUi(uiState)
        }
    }

    private fun addItemToList(itemText: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.ADD_ITEM, mapOf(Pair(AnalyticsValue.ValueName.NAME, itemText)))
        val selectedList = model.selectedList ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.addItemToShuffleList(selectedList.name, itemText)
                .catch { Timber.w(it) }.single()
            val newSelectedList = selectedList.copy(items = selectedList.items + ShuffleItem(itemText))
            model.selectedList = newSelectedList
            updateSelectedItems()
            val uiState = generateMainUiState()
                .copy(overlayView = UiState.OverlayView.EditListView(newSelectedList))
            updateUi(uiState)
        }
    }

    private fun requestDeleteList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.REQUEST_DELETE_LIST, mapOf(Pair(AnalyticsValue.ValueName.NAME, name)))
        _uiState.update {
            it.copy(overlayView = UiState.OverlayView.ConfirmDeleteListView(name))
        }
    }

    private fun confirmDeleteList(name: String) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.CONFIRM_DELETE_LIST)
        viewModelScope.launch(Dispatchers.IO) {
            deleteList(name)
            updateSelectedList()
            updateSelectedItems()
            updateUi()
        }
    }

    private suspend fun deleteList(name: String) {
        model.allListNames.remove(name)
        shuffleListRepository.deleteShuffleList(name).catch { Timber.w(it) }.single()
    }

    private suspend fun updateSelectedList() {
        val currentIndex = shuffleListRepository.getCurrentSelectedListIndex().first()
        val newIndex = max(0, min(currentIndex, model.allListNames.size - 1))
        if (currentIndex != newIndex) {
            shuffleListRepository.setCurrentSelectedListIndex(newIndex).single()
        }
        findSelectedList()
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
        val currentSubsetSize = model.selectedItems.size
        val newSubsetSize = currentSubsetSize + 1

        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DECREASE_SUBSET_SIZE,
            mapOf(Pair(AnalyticsValue.ValueName.OLD_VALUE, currentSubsetSize.toString()),
                Pair(AnalyticsValue.ValueName.NEW_VALUE, newSubsetSize.toString())))

        val list = model.selectedList?.also {
            Timber.e("Attempted to increase subset size when no list is selected")
        } ?: return

        val excludedItems = model.selectedItems.toSet()
        val available = list.items.filter { it !in excludedItems }
        if (available.isEmpty()) return

        val newItem = available.random()
        model.selectedList = list.copy(subsetSize = newSubsetSize)
        model.selectedItems.add(newItem)

        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.incListSubsetSize(list.name)
                .catch { Timber.w(it) }
                .single()
        }

        _uiState.update { state ->
            (state.mainView as? UiState.MainView.ShuffleView)?.let {
                state.copy(mainView = it.copy(
                    selectedItems = it.selectedItems + newItem,
                    numberOfSubsetItems = newSubsetSize,
                ))
            } ?: state
        }
    }

    private fun decreaseSubsetSize() {
        val currentSubsetSize = model.selectedItems.size
        val newSubsetSize = currentSubsetSize - 1

        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.DECREASE_SUBSET_SIZE,
            mapOf(Pair(AnalyticsValue.ValueName.OLD_VALUE, currentSubsetSize.toString()),
                Pair(AnalyticsValue.ValueName.NEW_VALUE, newSubsetSize.toString())))

        val list = model.selectedList?.also {
            Timber.e("Attempted to decrease subset size when no list is selected")
        } ?: return

        model.selectedList = list.copy(subsetSize = newSubsetSize)
        model.selectedItems.removeLastOrNull()

        viewModelScope.launch(Dispatchers.IO) {
            shuffleListRepository.decListSubsetSize(list.name)
                .catch { Timber.w(it) }
                .single()
        }

        _uiState.update { state ->
            (state.mainView as? UiState.MainView.ShuffleView)?.let {
                state.copy(mainView = it.copy(
                    selectedItems = it.selectedItems.dropLast(1),
                    numberOfSubsetItems = newSubsetSize,
                ))
            } ?: state
        }
    }

    private fun shuffleSelectedItem(index: Int) {
        analyticsLogger.logButtonTap(AnalyticsValue.ButtonName.SELECT_ITEM)
        val list = model.selectedList ?: return

        val excludedItems = model.selectedItems.toSet()
        val available = list.items.filter { it !in excludedItems }
        if (available.isEmpty()) return

        val newItem = available.random()
        model.selectedItems[index] = newItem

        _uiState.update { state ->
            (state.mainView as? UiState.MainView.ShuffleView)?.let {
                state.copy(mainView = it.copy(selectedItems = model.selectedItems.toList()))
            } ?: state
        }
    }
}