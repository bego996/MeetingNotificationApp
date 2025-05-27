package com.example.meetingnotification.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.meetingnotification.ui.MettingTopAppBar
import com.example.meetingnotification.ui.R
import com.example.meetingnotification.ui.navigation.NavigationDestination
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
) {
    val currentLocale = Locale.getDefault().language

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
