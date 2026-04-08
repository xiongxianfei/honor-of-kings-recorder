package com.xiongxianfei.honorkingsrecorder.ui.screens.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(vm: RecordViewModel = hiltViewModel()) {
    val form by vm.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { vm.importFromScreenshot(it) } }

    LaunchedEffect(form.saved) {
        if (form.saved) {
            snackbarHostState.showSnackbar("记录已保存！")
            vm.onSaveAcknowledged()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("记录对局", style = MaterialTheme.typography.headlineMedium)

        // ── Screenshot import ────────────────────────────────────────────────
        OutlinedButton(
            onClick = { pickImage.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !form.isParsingImage
        ) {
            if (form.isParsingImage) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(8.dp))
                Text("正在识别截图…")
            } else {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("从截图导入")
            }
        }

        if (form.imageParseHint.isNotEmpty()) {
            Text(
                text = form.imageParseHint,
                style = MaterialTheme.typography.bodySmall,
                color = if (form.imageParseHint.startsWith("已识别"))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
        }

        // ── Hero dropdown ────────────────────────────────────────────────────
        var heroExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = heroExpanded,
            onExpandedChange = { heroExpanded = it }
        ) {
            OutlinedTextField(
                value = form.hero,
                onValueChange = {},
                readOnly = true,
                label = { Text("英雄") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(heroExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = heroExpanded,
                onDismissRequest = { heroExpanded = false }
            ) {
                HEROES.forEach { hero ->
                    DropdownMenuItem(
                        text = { Text(hero) },
                        onClick = {
                            vm.onHeroChange(hero)
                            heroExpanded = false
                        }
                    )
                }
            }
        }

        // ── Win/Loss toggle ──────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = form.isWin,
                onClick = { vm.onWinChange(true) },
                label = { Text("胜利") }
            )
            FilterChip(
                selected = !form.isWin,
                onClick = { vm.onWinChange(false) },
                label = { Text("失败") }
            )
        }

        // ── Economy & Deaths ─────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = form.economyText,
                onValueChange = vm::onEconomyChange,
                label = { Text("经济") },
                placeholder = { Text("≥6500 得15分") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = form.deathsText,
                onValueChange = vm::onDeathsChange,
                label = { Text("死亡次数") },
                placeholder = { Text("≤2 得10分") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        // ── Checkboxes ───────────────────────────────────────────────────────
        Text("评分项（各+10分）", style = MaterialTheme.typography.titleSmall)
        CheckboxRow("抢/打了大龙", form.killedBaron, vm::onKilledBaronChange)
        CheckboxRow("通过三个问题检查", form.threeQuestionCheck, vm::onThreeQuestionChange)
        CheckboxRow("依托队友", form.reliedOnTeam, vm::onReliedOnTeamChange)
        CheckboxRow("推塔", form.pushedTower, vm::onPushedTowerChange)
        CheckboxRow("对线最强对手", form.engagedStrongest, vm::onEngagedStrongestChange)

        Text("评分项（各+15分）", style = MaterialTheme.typography.titleSmall)
        CheckboxRow("心态稳定", form.mentalStability, vm::onMentalStabilityChange)

        // ── Notes ────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = form.notes,
            onValueChange = vm::onNotesChange,
            label = { Text("备注（有内容+10分）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // ── Live score preview ───────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("本局得分预览", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${form.score} / 100",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = vm::save,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存记录")
        }

        Spacer(Modifier.height(8.dp))
        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
