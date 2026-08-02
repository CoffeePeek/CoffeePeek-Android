package com.coffeepeek.admin.ui.screen.brew

import com.coffeepeek.admin.base.BaseViewModel
import com.coffeepeek.admin.ui.Navigator
import com.coffeepeek.domain.model.BeanBag
import com.coffeepeek.domain.model.BrewMethod
import com.coffeepeek.domain.model.NewBrewSessionInput
import com.coffeepeek.domain.model.TasteTag
import com.coffeepeek.domain.repository.BrewRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class NewBrewStep {
    METHOD,
    BEAN,
    PARAMS,
    TASTE,
    ADVICE,
}

data class NewBrewUiState(
    val step: NewBrewStep = NewBrewStep.METHOD,
    val methods: List<BrewMethod> = BrewMethod.entries,
    val method: BrewMethod = BrewMethod.ESPRESSO,
    val beans: List<BeanBag> = emptyList(),
    val selectedBeanId: String? = null,
    val doseG: String = defaultDose(BrewMethod.ESPRESSO),
    val yieldOrWaterG: String = defaultYield(BrewMethod.ESPRESSO),
    val temperatureC: String = "",
    val grindNote: String = "",
    val notes: String = "",
    val tasteTags: Set<TasteTag> = emptySet(),
    val overallScore: Int? = null,
    val timerRunning: Boolean = false,
    val elapsedSec: Int = 0,
    val advicePreview: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

class NewBrewViewModel(
    private val brewRepository: BrewRepository,
    private val repeatSessionId: String = "",
) : BaseViewModel() {

    private val _state = MutableStateFlow(NewBrewUiState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    init {
        workScope.launch {
            val beans = brewRepository.getBeans()
            _state.update { it.copy(beans = beans) }
            if (repeatSessionId.isNotBlank()) {
                brewRepository.getSession(repeatSessionId)?.let { details ->
                    val s = details.session
                    _state.update {
                        it.copy(
                            method = s.method,
                            selectedBeanId = s.beanId,
                            doseG = s.doseG.toString().trimEnd('0').trimEnd('.'),
                            yieldOrWaterG = s.yieldOrWaterG.toString().trimEnd('0').trimEnd('.'),
                            temperatureC = s.temperatureC?.toString()?.trimEnd('0')?.trimEnd('.') ?: "",
                            grindNote = s.grindNote,
                            notes = s.notes,
                            tasteTags = s.tasteTags.toSet(),
                            overallScore = s.overallScore,
                            elapsedSec = s.durationSec,
                            step = NewBrewStep.PARAMS,
                        )
                    }
                }
            }
        }
    }

    fun selectMethod(method: BrewMethod) {
        _state.update {
            it.copy(
                method = method,
                doseG = defaultDose(method),
                yieldOrWaterG = defaultYield(method),
            )
        }
    }

    fun selectBean(beanId: String?) {
        _state.update { it.copy(selectedBeanId = beanId) }
    }

    fun onDose(value: String) = _state.update { it.copy(doseG = value.filterNumeric()) }
    fun onYield(value: String) = _state.update { it.copy(yieldOrWaterG = value.filterNumeric()) }
    fun onTemp(value: String) = _state.update { it.copy(temperatureC = value.filterNumeric()) }
    fun onGrind(value: String) = _state.update { it.copy(grindNote = value) }
    fun onNotes(value: String) = _state.update { it.copy(notes = value) }

    fun toggleTaste(tag: TasteTag) {
        _state.update {
            val next = it.tasteTags.toMutableSet()
            if (!next.add(tag)) next.remove(tag)
            it.copy(tasteTags = next)
        }
    }

    fun setScore(score: Int) {
        _state.update { it.copy(overallScore = score.coerceIn(1, 5)) }
    }

    fun startTimer() {
        if (_state.value.timerRunning) return
        _state.update { it.copy(timerRunning = true) }
        timerJob?.cancel()
        timerJob = workScope.launch {
            while (isActive && _state.value.timerRunning) {
                delay(1000)
                if (_state.value.timerRunning) {
                    _state.update { it.copy(elapsedSec = it.elapsedSec + 1) }
                }
            }
        }
    }

    fun pauseTimer() {
        _state.update { it.copy(timerRunning = false) }
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _state.update { it.copy(elapsedSec = 0) }
    }

    fun next() {
        val current = _state.value
        when (current.step) {
            NewBrewStep.METHOD -> _state.update { it.copy(step = NewBrewStep.BEAN, error = null) }
            NewBrewStep.BEAN -> _state.update { it.copy(step = NewBrewStep.PARAMS, error = null) }
            NewBrewStep.PARAMS -> {
                val dose = current.doseG.toFloatOrNull()
                val yield = current.yieldOrWaterG.toFloatOrNull()
                if (dose == null || dose <= 0f || yield == null || yield <= 0f) {
                    _state.update { it.copy(error = "Укажите дозу и выход/воду") }
                    return
                }
                pauseTimer()
                _state.update { it.copy(step = NewBrewStep.TASTE, error = null) }
            }
            NewBrewStep.TASTE -> {
                val advice = brewRepository.previewAdvice(buildInput())
                _state.update { it.copy(step = NewBrewStep.ADVICE, advicePreview = advice, error = null) }
            }
            NewBrewStep.ADVICE -> save()
        }
    }

    fun back() {
        val current = _state.value.step
        val previous = when (current) {
            NewBrewStep.METHOD -> {
                Navigator.popBack()
                return
            }
            NewBrewStep.BEAN -> NewBrewStep.METHOD
            NewBrewStep.PARAMS -> NewBrewStep.BEAN
            NewBrewStep.TASTE -> NewBrewStep.PARAMS
            NewBrewStep.ADVICE -> NewBrewStep.TASTE
        }
        _state.update { it.copy(step = previous, error = null) }
    }

    fun save() {
        if (_state.value.isSaving) return
        workScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            runCatching { brewRepository.createSession(buildInput()) }
                .onSuccess { session ->
                    _state.update { it.copy(isSaving = false) }
                    Navigator.popThenNavigate(Navigator.Screen.BrewDetail(session.id))
                }
                .onFailure { e ->
                    _state.update { it.copy(isSaving = false, error = e.message ?: "Не удалось сохранить") }
                }
        }
    }

    private fun buildInput(): NewBrewSessionInput {
        val s = _state.value
        return NewBrewSessionInput(
            beanId = s.selectedBeanId,
            method = s.method,
            doseG = s.doseG.toFloatOrNull() ?: 0f,
            yieldOrWaterG = s.yieldOrWaterG.toFloatOrNull() ?: 0f,
            durationSec = s.elapsedSec,
            temperatureC = s.temperatureC.toFloatOrNull(),
            grindNote = s.grindNote,
            tasteTags = s.tasteTags.toList(),
            overallScore = s.overallScore,
            notes = s.notes,
        )
    }

    override fun close() {
        timerJob?.cancel()
        super.close()
    }
}

private fun String.filterNumeric(): String =
    filterIndexed { index, c -> c.isDigit() || (c == '.' && index > 0 && !contains('.')) || (c == ',' && index > 0) }
        .replace(',', '.')
