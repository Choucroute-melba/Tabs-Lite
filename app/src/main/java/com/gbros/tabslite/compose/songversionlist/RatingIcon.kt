package com.gbros.tabslite.compose.songversionlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gbros.tabslite.R
import com.gbros.tabslite.ui.theme.AppTheme
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun RatingIcon(rating: Double){
    var filledStars = floor(rating).toInt()
    var unfilledStars = (5 - ceil(rating)).toInt()
    var halfStar = false
    val remainder = rating.rem(1)

    // round to the nearest half star
    if (remainder > 0) {
        if (remainder >= .8) filledStars++
        else if (remainder < .25) unfilledStars++
        else halfStar = true
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ){
        repeat(filledStars) {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Filled star", tint = MaterialTheme.colorScheme.primary)
        }

        if (halfStar) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_left_half),
                contentDescription = "Half star",
                tint = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_rating_star_right_half),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background
            )
        }

        repeat(unfilledStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,

                )
        }
    }
}

@Composable @Preview
private fun RatingIconPreview(){
    AppTheme {
        Column {
            RatingIcon(5.0)
            RatingIcon(4.9)
            RatingIcon(4.7)
            RatingIcon(4.1)
            RatingIcon(0.9)
            RatingIcon(0.5)
            RatingIcon(0.1)
            RatingIcon(0.0)
        }
    }
}