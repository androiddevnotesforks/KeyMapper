package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.architecturetest.data.KeymapRepository
import io.github.sds100.keymapper.data.model.Action
import io.github.sds100.keymapper.data.model.KeyMap
import io.github.sds100.keymapper.data.model.Trigger
import kotlinx.coroutines.launch

open class ConfigKeymapViewModel internal constructor(
    private val repository: KeymapRepository,
    val id: Long? = null
) : ViewModel() {

    val triggerKeys: MutableLiveData<List<Trigger.Key>> = MutableLiveData()
    val triggerInParallel: MutableLiveData<Boolean> = MutableLiveData(false)
    val triggerInSequence: MutableLiveData<Boolean> = MutableLiveData(false)
    val actionList: MutableLiveData<List<Action>> = MutableLiveData()
    val flags: MutableLiveData<Int> = MutableLiveData()
    val isEnabled: MutableLiveData<Boolean> = MutableLiveData()

    init {
        if (id == null) {
            triggerKeys.value = listOf()
            actionList.value = listOf()
            flags.value = 0
            isEnabled.value = true

            when (Trigger.DEFAULT_TRIGGER_MODE) {
                Trigger.PARALLEL -> triggerInParallel.value = true
                Trigger.SEQUENCE -> triggerInSequence.value = true
            }

        } else {
            viewModelScope.launch {
                repository.getKeymap(id).let { keymap ->
                    triggerKeys.value = keymap.trigger?.keys
                    actionList.value = keymap.actionList
                    flags.value = keymap.flags
                    isEnabled.value = keymap.isEnabled

                    when (keymap.trigger?.mode) {
                        Trigger.PARALLEL -> triggerInParallel.value = true
                        Trigger.SEQUENCE -> triggerInSequence.value = true
                    }
                }
            }
        }
    }

    fun saveKeymap() {
        viewModelScope.launch {
            val triggerMode = when {
                triggerInParallel.value == true -> Trigger.PARALLEL
                triggerInSequence.value == true -> Trigger.SEQUENCE
                else -> Trigger.DEFAULT_TRIGGER_MODE
            }

            val keymap = KeyMap(
                id = id ?: 0,
                trigger = Trigger(triggerKeys.value!!, triggerMode),
                actionList = actionList.value!!,
                flags = flags.value!!,
                isEnabled = isEnabled.value!!
            )

            if (id == null) {
                repository.createKeymap(keymap)
            } else {
                repository.updateKeymap(keymap)
            }
        }
    }

    class Factory(
        private val mRepository: KeymapRepository, private val mId: Long
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigKeymapViewModel(mRepository, mId) as T
    }
}