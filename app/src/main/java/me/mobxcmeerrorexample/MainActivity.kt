package me.mobxcmeerrorexample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import mobx.core.action
import mobx.core.observable
import mobx.core.whenThen
import kotlin.concurrent.fixedRateTimer

class TestStore {
  var count by observable(0)
    private set

  fun incCount() = action {
    count += 1
  }

  fun decCount() = action {
    count -= 1
  }
}



class MainActivity : AppCompatActivity() {
  private var countView: TextView? = null

  val testStore = TestStore()

  @SuppressLint("MissingInflatedId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    countView = findViewById<TextView>(R.id.text)

    observeChanges({testStore.count}) {
      runOnUiThread {
        countView?.text = "Count is $it"
      }
    }

    initIncUpdates()
    initDecUpdates()
  }

  private fun initDecUpdates() {
    fixedRateTimer("inc", false, 0L, 200L ) {
      testStore.decCount()
    }
  }

  private fun initIncUpdates() {
    fixedRateTimer("inc", false, 0L, 100L ) {
      testStore.incCount()
    }
  }
}