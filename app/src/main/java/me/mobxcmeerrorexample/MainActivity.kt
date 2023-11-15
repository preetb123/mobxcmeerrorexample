package me.mobxcmeerrorexample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import mobx.core.action
import java.util.Comparator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.KMutableProperty1

class TestStore {
  var count = 0
    private set
  var mySet = ObservableSet(ConcurrentSkipListSet<User>(Comparator.comparing {
    it.name
  }))

  fun incCount() = action {
    count += 1
    mySet.add(User(count, "User $count"))
  }

  fun decCount() = action {
    count -= 1
    mySet.add(User(count, "User $count"))
  }
}

interface OnListChangeListener {
  fun onChanged()
}

private val listeners = mutableSetOf<OnListChangeListener>()

class ObservableSet<User>(private val set: MutableSet<User>) : MutableSet<User> by set {
  private lateinit var indexProperty: KMutableProperty1<User, Int>
  private var map: Map<Int, User?>? = null
  override fun add(element: User): Boolean {
    val res = set.add(element)
    if(res) {
      Log.d("SET_ADD", "add() called with: element = $element, allElements: $set")
      for(listener in listeners){
        listener.onChanged()
      }
    }
    return res
  }

  override fun addAll(elements: Collection<User>): Boolean {
    if (indexProperty != null) {
      map = elements.associateBy { indexProperty.get(it) }
    }
    return set.addAll(elements)
  }

  fun get(age: Int): User? {
    return map!![age]
  }

  fun addListener(listener: OnListChangeListener) {
    listeners.add(listener)
  }

  fun setIndexProperty(kMutableProperty1: KMutableProperty1<User, Int>) {
    indexProperty = kMutableProperty1
  }
}

data class Test(val name: String)

class User{
  var age: Int=0
  var name: String=""

  constructor(age: Int, name: String) {
    this.age = age
    this.name = name
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as User

    if (age != other.age) return false
    if (name != other.name) return false

    return true
  }

  override fun hashCode(): Int {
    var result = age
    result = 31 * result + name.hashCode()
    return result
  }

  override fun toString(): String {
    return "User(age=$age, name='$name')"
  }
}

class MainActivity : AppCompatActivity() {
  private var countView: TextView? = null

  val testStore = TestStore()
  val userSet = ObservableSet(ConcurrentSkipListSet<User>(Comparator.comparing {
    it.name
  }))
  @SuppressLint("MissingInflatedId")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    countView = findViewById<TextView>(R.id.text)
    userSet.setIndexProperty(User::age)

//    observeChanges({testStore.count}) {
//      runOnUiThread {
//        countView?.text = "Count is $it"
//      }
//    }

    userSet.addAll(buildSet {
      add(User(1, "hjhdjf"))
      add(User(2, "fsdmk"))
      add(User(3, "hjhdjf"))
      add(User(4, "ooo"))
      add(User(5, "afd"))
      add(User(6, "fd"))
      add(User(7, "ee"))
    })


    userSet.addListener(object: OnListChangeListener {
      override fun onChanged() {
        Log.d("ON_CHANGE", "onChanged() called")
      }
    })
    Log.d("SET_CONTENT", ""+userSet.get(5))
    userSet.toMutableList()


    initIncUpdates()
    initDecUpdates()
  }

  private fun initDecUpdates() {
    fixedRateTimer("inc", false, 0L, 3000 ) {
      testStore.decCount()

    }
  }

  private fun initIncUpdates() {
    fixedRateTimer("inc", false, 0L, 2000 ) {
      testStore.incCount()
    }
  }

}
