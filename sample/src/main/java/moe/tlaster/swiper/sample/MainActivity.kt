package moe.tlaster.swiper.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.swiper.Direction
import moe.tlaster.swiper.Swiper
import moe.tlaster.swiper.SwiperState
import moe.tlaster.swiper.rememberSwiperState
import java.util.UUID

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold {
                  Surface (
                      modifier = Modifier.padding(it)
                  ){
                      Column(
                          modifier = Modifier.fillMaxSize(),
                          verticalArrangement = Arrangement.Center,
                          horizontalAlignment = Alignment.CenterHorizontally

                      ) {
                          val pages = remember { mutableStateListOf<String>() }
                          val pagerState = rememberPagerState(0) {pages.size}
                          HorizontalPager(
                              pagerState,
                              modifier = Modifier.fillMaxWidth().fillMaxHeight(.7f),
                              contentPadding = PaddingValues(horizontal = 100.dp),
                              pageSpacing = 10.dp,
                          ) {
                              index ->
                              Swiper(
                                  enabled = true,
                                  direction = Direction.Up,
                                  orientation = Orientation.Vertical,
                                  dismissHeight = .5f,
                                  state = rememberSwiperState (key = UUID.randomUUID().toString(),
                                      onDismiss = {
                                          pages.remove(index.toString())
                                      }
                                  )
                              ) {
                                  Surface(
                                      modifier = Modifier.fillMaxSize(),
                                      color = Color.Gray
                                  ) {
                                     Column {
                                         Text(if(pages.isNotEmpty()) "page" else "No Pages", color = Color.White )
                                         Button(onClick = {
                                             pages.remove(pages.size.toString())
                                         }) {
                                             Text("Delete")
                                         }
                                     }
                                  }
                              }
                          }

                          Row {
                              Button(onClick = {
                                  pages.add(pages.size.toString())
                              }) {
                                  Text("Add New ")
                              }
                          }
                      }
                  }

                }
            }
        }
    }
}
