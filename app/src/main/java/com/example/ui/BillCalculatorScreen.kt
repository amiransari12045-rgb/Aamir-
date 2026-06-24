package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavedBill
import com.example.ui.theme.CustomDanger
import com.example.ui.theme.CustomSuccess
import com.example.ui.theme.CustomOrange
import com.example.utils.BillExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCalculatorScreen(
    viewModel: BillViewModel,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val finalConsumption by viewModel.finalConsumption.collectAsStateWithLifecycle()
    val previousMonth by viewModel.previousMonth.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val billMonth by viewModel.billMonth.collectAsStateWithLifecycle()
    val billYear by viewModel.billYear.collectAsStateWithLifecycle()
    val calculationResult by viewModel.calculationResult.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val savedBills by viewModel.savedBills.collectAsStateWithLifecycle()

    val submeters = viewModel.submeters
    val scrollState = rememberScrollState()

    // Show validation error toast or alert
    LaunchedEffect(validationError) {
        validationError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val baseColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFEEF2FF)
                drawRect(color = baseColor)

                val glow1Color = if (isDarkMode) Color(0x3B8B5CF6) else Color(0x33A5B4FC) // Purple/Indigo glow
                val glow2Color = if (isDarkMode) Color(0x3B0EA5E9) else Color(0x3399F6E4) // Cyan/Teal glow

                // Top-left glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow1Color, Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = size.minDimension * 0.9f
                    ),
                    radius = size.minDimension * 0.9f,
                    center = androidx.compose.ui.geometry.Offset(0f, 0f)
                )

                // Bottom-right glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow2Color, Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        radius = size.minDimension * 0.9f
                    ),
                    radius = size.minDimension * 0.9f,
                    center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Electric ⚡",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFF8B5CF6) else Color(0xFF4F46E5),
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Bill Calculator Pro",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkMode) Color(0xFFF9FAF8) else Color(0xFF111827)
                        )
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isDarkMode) {
                                    listOf(Color(0xFF8B5CF6), Color(0xFF0EA5E9))
                                } else {
                                    listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
                                }
                            )
                        )
                ) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            // HEADER HERO CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Divide. Adjust. Save.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Accurately split main meter charges among multiple rooms, adjusting submeter variance automatically.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MAIN BILL CONFIG CARD
            Text(
                text = "Main Meter Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FrostedGlassCard(
                isDarkMode = isDarkMode,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Row 1: Total Amount & Final consumption
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = totalAmount,
                            onValueChange = { viewModel.updateTotalAmount(it) },
                            label = { Text("Total Amount Payable") },
                            leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) },
                            colors = frostedTextFieldColors(isDarkMode),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("total_amount_input")
                        )

                        OutlinedTextField(
                            value = finalConsumption,
                            onValueChange = { viewModel.updateFinalConsumption(it) },
                            label = { Text("Main Consumption Unit") },
                            colors = frostedTextFieldColors(isDarkMode),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("final_consumption_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 2: Previous Month & Current Month Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MonthDropdownSelector(
                            label = "Previous Month",
                            selectedMonth = previousMonth,
                            onMonthSelected = { viewModel.updatePreviousMonth(it) },
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )

                        MonthDropdownSelector(
                            label = "Current Month",
                            selectedMonth = currentMonth,
                            onMonthSelected = { viewModel.updateCurrentMonth(it) },
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 3: Target Bill Month & Year
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MonthDropdownSelector(
                            label = "Bill Month",
                            selectedMonth = billMonth,
                            onMonthSelected = { viewModel.updateBillMonth(it) },
                            isDarkMode = isDarkMode,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = billYear,
                            onValueChange = { viewModel.updateBillYear(it) },
                            label = { Text("Bill Year") },
                            colors = frostedTextFieldColors(isDarkMode),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SUBMETERS INPUT LIST
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Submeter Inputs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = { viewModel.addSubmeter() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0x338B5CF6) else Color(0x1F4F46E5),
                        contentColor = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add submeter", size = 16.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Submeter", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            submeters.forEachIndexed { index, submeter ->
                FrostedGlassCard(
                    isDarkMode = isDarkMode,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Submeter #${index + 1}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
                                )
                            )

                            if (submeters.size > 2) {
                                IconButton(
                                    onClick = { viewModel.removeSubmeter(submeter.id) },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = CustomDanger)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete submeter")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = submeter.name,
                            onValueChange = { viewModel.updateSubmeterName(submeter.id, it) },
                            label = { Text("Submeter / Room Name") },
                            colors = frostedTextFieldColors(isDarkMode),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = submeter.previousReading,
                                onValueChange = { viewModel.updateSubmeterPrevious(submeter.id, it) },
                                label = { Text("Previous Reading") },
                                colors = frostedTextFieldColors(isDarkMode),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = submeter.currentReading,
                                onValueChange = { viewModel.updateSubmeterCurrent(submeter.id, it) },
                                label = { Text("Current Reading") },
                                colors = frostedTextFieldColors(isDarkMode),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CALCULATE BUTTON
            Button(
                onClick = { viewModel.calculateBill() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF16A34A), Color(0xFF22C55E))
                        )
                    )
                    .testTag("calculate_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Calculate")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Bill", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BILL CALCULATION RESULT SECTION
            AnimatedVisibility(
                visible = calculationResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                calculationResult?.let { result ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Calculation Result",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        FrostedGlassCard(
                            isDarkMode = isDarkMode,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Electric Bill ${result.billMonth} ${result.billYear}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5),
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                )

                                // Breakdown Summary Card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isDarkMode) Color(0x228B5CF6) else Color(0x1F4F46E5))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Units", fontSize = 11.sp, color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000))
                                        Text(
                                            text = String.format("%.2f", result.finalConsumption),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color(0xFF111827)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Amount", fontSize = 11.sp, color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000))
                                        Text(
                                            text = "₹" + String.format("%.2f", result.totalAmount),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color(0xFF111827)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Unit Rate", fontSize = 11.sp, color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000))
                                        Text(
                                            text = "₹" + String.format("%.4f", result.perUnitRate),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color.White else Color(0xFF111827)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider(color = if (isDarkMode) Color(0x26FFFFFF) else Color(0x1A000000))

                                Spacer(modifier = Modifier.height(12.dp))

                                // Submeter detail boxes
                                result.submeters.forEach { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isDarkMode) Color(0x14FFFFFF) else Color(0x26FFFFFF))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${result.previousMonth} Reading: ${item.previousReading}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${result.currentMonth} Reading: ${item.currentReading}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Original Units: ${String.format("%.1f", item.rawUnits)}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Final Adjusted Units: ${String.format("%.2f", item.finalUnits)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Amount Owed:",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "₹" + String.format("%.2f", item.calculatedAmount),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CustomSuccess
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = if (isDarkMode) Color(0x26FFFFFF) else Color(0x1A000000))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Math verification footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Submeters Unit Sum:",
                                        fontSize = 11.sp,
                                        color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                                    )
                                    Text(
                                        text = String.format("%.2f", result.totalSubmeterUnits) + " Units",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Submeters Amount Sum:",
                                        fontSize = 11.sp,
                                        color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                                    )
                                    Text(
                                        text = "₹" + String.format("%.2f", result.totalSubmeterAmount),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // EXPORT BUTTONS ROW
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { BillExporter.shareAsText(context, result) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDarkMode) Color(0x228B5CF6) else Color(0x1F4F46E5),
                                            contentColor = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share text", size = 16.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share Text", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { BillExporter.shareAsPdf(context, result) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDarkMode) Color(0x220EA5E9) else Color(0x1F06B6D4),
                                            contentColor = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0EA5E9)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Share PDF", size = 16.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share PDF", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.saveBill()
                                            Toast.makeText(context, "Bill Saved Successfully", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isDarkMode) Color(0x22F97316) else Color(0x1FF97316),
                                            contentColor = if (isDarkMode) Color(0xFFFDBA74) else Color(0xFFEA580C)
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save Bill", size = 16.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Save Bill", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SAVED BILLS SECTION
            Text(
                text = "Saved Bills",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (savedBills.isEmpty()) {
                FrostedGlassCard(
                    isDarkMode = isDarkMode,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Saved Bills Yet",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                            )
                        )
                    }
                }
            } else {
                savedBills.forEach { bill ->
                    SavedBillItem(
                        bill = bill,
                        isDarkMode = isDarkMode,
                        onOpen = { viewModel.openBill(bill) },
                        onDelete = { viewModel.deleteBill(bill.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SavedBillItem(
    bill: SavedBill,
    isDarkMode: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    FrostedGlassCard(
        isDarkMode = isDarkMode,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${bill.billMonth} ${bill.billYear}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: ₹${String.format("%.2f", bill.totalAmount)} • ${bill.submeterBills.size} Submeters",
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color(0x99FFFFFF) else Color(0x99000000)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) Color(0x338B5CF6) else Color(0x1F4F46E5),
                        contentColor = if (isDarkMode) Color(0xFFA78BFA) else Color(0xFF4F46E5)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = CustomDanger)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Saved Bill")
                }
            }
        }
    }
}

@Composable
fun MonthDropdownSelector(
    label: String,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            colors = frostedTextFieldColors(isDarkMode),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Icon helper sizes
@Composable
fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Composable
fun FrostedGlassCard(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (isDarkMode) Color(0x1AFFFFFF) else Color(0x73FFFFFF) // bg-white/10 vs bg-white/45
    val borderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x4DFFFFFF) // border border-white/20 vs border border-white/25

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderColor, shape)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun frostedTextFieldColors(isDarkMode: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = if (isDarkMode) Color(0x13FFFFFF) else Color(0x1AFFFFFF),
    unfocusedContainerColor = if (isDarkMode) Color(0x05FFFFFF) else Color(0x0DFFFFFF),
    focusedBorderColor = if (isDarkMode) Color(0xFF8B5CF6) else Color(0xFF4F46E5),
    unfocusedBorderColor = if (isDarkMode) Color(0x26FFFFFF) else Color(0x33FFFFFF),
    focusedLabelColor = if (isDarkMode) Color(0xFF8B5CF6) else Color(0xFF4F46E5),
    unfocusedLabelColor = if (isDarkMode) Color(0x80FFFFFF) else Color(0x80000000),
    focusedTextColor = if (isDarkMode) Color.White else Color(0xFF111827),
    unfocusedTextColor = if (isDarkMode) Color.White else Color(0xFF111827)
)
