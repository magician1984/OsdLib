package idv.bruce.ui.osd

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val arr1:Array<Boolean> = Array(10){true}


        val arr2:Array<Boolean> = Array(3){true}
        arr1.fill(false,0,2)

        arr1.forEachIndexed { index : Int, b : Boolean ->
            print("Index : $index, $b\n")
        }

        val index = Collections.indexOfSubList(arr1.asList(), arr2.asList())
        print("Index : $index")
        assertEquals(4, 2 + 2)
    }
}