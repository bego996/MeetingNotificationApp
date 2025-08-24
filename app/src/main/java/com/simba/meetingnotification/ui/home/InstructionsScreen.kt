package com.simba.meetingnotification.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simba.meetingnotification.ui.AppViewModelProvider
import com.simba.meetingnotification.ui.MettingTopAppBar
import com.simba.meetingnotification.ui.R
import com.simba.meetingnotification.ui.navigation.NavigationDestination
import java.util.Locale

object InstructionsDestination : NavigationDestination {
    override val route: String = "help"
    override val titleRes: Int = R.string.instructions
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(
    onBack: () -> Unit,
    modifier: Modifier,
    viewModel: InstructionsScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateToHome: () -> Unit
) {
    val currentLocale = Locale.getDefault().language
    val instructionReadState = viewModel.instructionReadState.collectAsState()

    Scaffold(
        topBar = {
            MettingTopAppBar(
                modifier = modifier,
                title = stringResource(InstructionsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = { onBack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HelpSection(stringResource(R.string.help_title_functionality), stringResource(R.string.help_functionality).trimIndent(), null)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                InstructionsScreenSettingsLink()
                Image(
                    painter = painterResource(if (currentLocale == "de") R.drawable.app_settings_de else R.drawable.app_settings_en),
                    contentDescription = "title",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            HelpSectionFirstSteps(
                stringResource(R.string.help_title_first_steps),
                stringResource(R.string.help_first_steps_contacts).trimIndent(),
                if (currentLocale == "de") R.drawable.addcontact else R.drawable.addcontacten,
                stringResource(R.string.help_first_steps_events).trimIndent(),
                if (currentLocale == "de") R.drawable.addevent else R.drawable.addeventen
            )
            HelpSection("\n${stringResource(R.string.help_title_saved_contacts)}", stringResource(R.string.help_saved_contacts).trimIndent(),if (currentLocale == "de") R.drawable.savedcontact else R.drawable.savedcontacten)
            HelpSection("\n${stringResource(R.string.help_title_search_contacts)}", stringResource(R.string.help_search_contacts).trimIndent(),if (currentLocale == "de") R.drawable.searchcontact else R.drawable.searchcontacten)
            HelpSection("\n${stringResource(R.string.help_title_template_check)}", stringResource(R.string.help_template_check).trimIndent(),if (currentLocale == "de") R.drawable.templatecheckde else R.drawable.templatechecken)
            HelpSectionChangeMessage(stringResource(R.string.help_template_edit).trimIndent(),if (currentLocale == "de") R.drawable.templatechange else R.drawable.templatechangeen)
            HelpSection("\n${stringResource(R.string.help_title_home)}", stringResource(R.string.help_home_screen).trimIndent(),if (currentLocale == "de")  R.drawable.homescreen else R.drawable.homescreenen)
            HelpSection("\n${stringResource(R.string.help_title_refresh)}", stringResource(R.string.help_refresh_info).trimIndent(),null)

            Spacer(modifier = Modifier.height(15.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BB625)),
                    onClick = {
                        if (!instructionReadState.value) {
                            viewModel.setInstructionToReaden()
                            navigateToHome()
                        } else
                            navigateToHome()
                    },
                ) {
                    Text(stringResource(R.string.acknowledge))
                }
            }
        }
    }
}


@Composable
fun HelpSection(title: String,description: String,picRes:Int?) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(picRes)
    }
}

@Composable
fun InstructionsScreenSettingsLink() {
    val context = LocalContext.current
    Text(
        buildAnnotatedString {
            append(stringResource(R.string.help_settings_for_alarm_notification))
            pushStyle(
                SpanStyle(
                    color = Color(0xFF1BB625),
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold
                )
            )
            append(stringResource(R.string.help_settings_for_alarm_notification_link_redirect))
            pop()
        },
        modifier = Modifier
            .clickable {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            .padding(vertical = 8.dp)
    )
}

@Composable
fun HelpSectionChangeMessage(
    addMessageChangeDescription: String,addMessagePic:Int?
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(addMessageChangeDescription, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addMessagePic)
    }
}

@Composable
fun HelpSectionFirstSteps(
    title: String,
    addContactDescription: String,addContactPic:Int?,
    addEventDescription: String, addEventPic:Int?
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(addContactDescription, style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addContactPic)
        Text(addEventDescription,style = MaterialTheme.typography.bodyMedium)
        ImageHelper(addEventPic)
    }
}

@Composable
fun ImageHelper(imageRes:Int?){
    imageRes?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(id = it),
            contentDescription = "title",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}
