package com.xiongxianfei.honorkingsrecorder.ui.screens.record

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.xiongxianfei.honorkingsrecorder.data.model.Match
import com.xiongxianfei.honorkingsrecorder.data.repository.MatchRepository
import com.xiongxianfei.honorkingsrecorder.util.ScoreCalculator
import com.xiongxianfei.honorkingsrecorder.util.ScreenshotParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val HEROES = listOf("后羿", "莱西奥", "艾琳", "戈娅", "孙尚香", "公孙离")

data class RecordFormState(
    val hero: String = HEROES.first(),
    val isWin: Boolean = true,
    val economyText: String = "",
    val killsText: String = "",
    val deathsText: String = "",
    val assistsText: String = "",
    val killedBaron: Boolean = false,
    val threeQuestionCheck: Boolean = false,
    val reliedOnTeam: Boolean = false,
    val pushedTower: Boolean = false,
    val engagedStrongest: Boolean = false,
    val mentalStability: Boolean = false,
    val notes: String = "",
    val saved: Boolean = false,
    val isParsingImage: Boolean = false,
    val imageParseHint: String = ""
) {
    val economy: Int get() = economyText.toIntOrNull() ?: 0
    val kills: Int   get() = killsText.toIntOrNull()   ?: 0
    val deaths: Int  get() = deathsText.toIntOrNull()  ?: 0
    val assists: Int get() = assistsText.toIntOrNull() ?: 0
    val score: Int get() = ScoreCalculator.calculate(
        economy, deaths, killedBaron, threeQuestionCheck, reliedOnTeam,
        pushedTower, engagedStrongest, mentalStability, notes
    )
}

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: MatchRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editMatchId: Long? = savedStateHandle.get<Long>("matchId")
    private var originalTimestamp: Long = System.currentTimeMillis()

    val isEditMode: Boolean get() = editMatchId != null

    private val _form = MutableStateFlow(RecordFormState())
    val form: StateFlow<RecordFormState> = _form.asStateFlow()

    init {
        if (editMatchId != null) {
            viewModelScope.launch {
                val match = repo.getById(editMatchId) ?: return@launch
                originalTimestamp = match.timestamp
                _form.value = RecordFormState(
                    hero = match.hero,
                    isWin = match.isWin,
                    economyText = if (match.economy == 0) "" else match.economy.toString(),
                    killsText = if (match.kills == 0) "" else match.kills.toString(),
                    deathsText = if (match.deaths == 0) "" else match.deaths.toString(),
                    assistsText = if (match.assists == 0) "" else match.assists.toString(),
                    killedBaron = match.killedBaron,
                    threeQuestionCheck = match.threeQuestionCheck,
                    reliedOnTeam = match.reliedOnTeam,
                    pushedTower = match.pushedTower,
                    engagedStrongest = match.engagedStrongest,
                    mentalStability = match.mentalStability,
                    notes = match.notes,
                )
            }
        }
    }

    fun onHeroChange(hero: String)       = _form.update { it.copy(hero = hero) }
    fun onWinChange(isWin: Boolean)      = _form.update { it.copy(isWin = isWin) }
    fun onEconomyChange(text: String)    = _form.update { it.copy(economyText = text.filter(Char::isDigit)) }
    fun onKillsChange(text: String)      = _form.update { it.copy(killsText   = text.filter(Char::isDigit)) }
    fun onDeathsChange(text: String)     = _form.update { it.copy(deathsText  = text.filter(Char::isDigit)) }
    fun onAssistsChange(text: String)    = _form.update { it.copy(assistsText = text.filter(Char::isDigit)) }
    fun onKilledBaronChange(v: Boolean)  = _form.update { it.copy(killedBaron = v) }
    fun onThreeQuestionChange(v: Boolean)= _form.update { it.copy(threeQuestionCheck = v) }
    fun onReliedOnTeamChange(v: Boolean) = _form.update { it.copy(reliedOnTeam = v) }
    fun onPushedTowerChange(v: Boolean)  = _form.update { it.copy(pushedTower = v) }
    fun onEngagedStrongestChange(v: Boolean) = _form.update { it.copy(engagedStrongest = v) }
    fun onMentalStabilityChange(v: Boolean)  = _form.update { it.copy(mentalStability = v) }
    fun onNotesChange(text: String)      = _form.update { it.copy(notes = text) }

    fun save() {
        val f = _form.value
        viewModelScope.launch {
            repo.insert(
                Match(
                    id = editMatchId ?: 0,
                    hero = f.hero,
                    timestamp = if (editMatchId != null) originalTimestamp else System.currentTimeMillis(),
                    isWin = f.isWin,
                    economy = f.economy,
                    kills = f.kills,
                    deaths = f.deaths,
                    assists = f.assists,
                    killedBaron = f.killedBaron,
                    threeQuestionCheck = f.threeQuestionCheck,
                    reliedOnTeam = f.reliedOnTeam,
                    pushedTower = f.pushedTower,
                    engagedStrongest = f.engagedStrongest,
                    mentalStability = f.mentalStability,
                    notes = f.notes,
                    score = f.score
                )
            )
            _form.update { it.copy(saved = true) }
        }
    }

    fun onSaveAcknowledged() = _form.update { it.copy(saved = false) }

    /** Runs ML Kit Chinese OCR on [uri] and pre-fills matching form fields. */
    fun importFromScreenshot(uri: Uri) {
        viewModelScope.launch {
            _form.update { it.copy(isParsingImage = true, imageParseHint = "") }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                if (bitmap == null) {
                    _form.update { it.copy(isParsingImage = false, imageParseHint = "无法读取图片") }
                    return@launch
                }

                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(
                    ChineseTextRecognizerOptions.Builder().build()
                )

                val visionText = suspendCancellableCoroutine { cont ->
                    recognizer.process(image)
                        .addOnSuccessListener { cont.resume(it) }
                        .addOnFailureListener { cont.resumeWithException(it) }
                }

                val parsed = ScreenshotParser.parse(visionText.text)
                val hints  = mutableListOf<String>()

                _form.update { f ->
                    var updated = f.copy(isParsingImage = false)
                    parsed.hero?.let    { updated = updated.copy(hero = it);                     hints += it }
                    parsed.isWin?.let   { updated = updated.copy(isWin = it);                    hints += if (it) "胜利" else "失败" }
                    parsed.economy?.let { updated = updated.copy(economyText = it.toString());   hints += "经济$it" }
                    parsed.kills?.let   { updated = updated.copy(killsText   = it.toString());   hints += "击杀$it" }
                    parsed.deaths?.let  { updated = updated.copy(deathsText  = it.toString());   hints += "死亡$it" }
                    parsed.assists?.let { updated = updated.copy(assistsText = it.toString());   hints += "助攻$it" }
                    val baseHint = if (hints.isEmpty()) "未识别到数据，请手动填写"
                                   else "已识别：${hints.joinToString(" · ")}"
                    // Append raw OCR snippet when economy couldn't be parsed (helps tuning)
                    val debugSuffix = parsed.rawOcrHint
                        ?.let { "\n[OCR] $it" } ?: ""
                    updated.copy(imageParseHint = baseHint + debugSuffix)
                }
            } catch (e: Exception) {
                _form.update { it.copy(isParsingImage = false, imageParseHint = "识别失败：${e.message}") }
            }
        }
    }
}
