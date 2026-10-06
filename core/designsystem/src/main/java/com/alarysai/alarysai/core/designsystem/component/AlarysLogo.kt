package com.alarysai.alarysai.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alarysai.alarysai.core.designsystem.R
import com.alarysai.alarysai.core.designsystem.theme.AlarysTheme

@Composable
fun AlarysLogo(
    modifier: Modifier = Modifier,
    markSize: Dp = 160.dp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlarysMark(
            modifier = Modifier.size(markSize)
        )
    }
}

@Composable
private fun AlarysMark(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = R.mipmap.ic_alarys),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
private fun AlarysLogoPreview() {
    AlarysTheme {
        AlarysLogo()
    }
}
