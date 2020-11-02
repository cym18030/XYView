package cym.xyview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 回调
        xy_view.setOnCallbackListener(object : XYView.OnCallbackListener {
            override fun onCallback(offsetX: Float, offsetY: Float) {
                val title = "当前（${getPreFloat(offsetX)},${getPreFloat(offsetY)}）"
                setTitle(title)
            }

            override fun onCallback(offsetX: Float, offsetY: Float, upOffsetX: Float, upOffsetY: Float) {
                val title = "当前（${getPreFloat(offsetX)},${getPreFloat(offsetY)}） 点击（${getPreFloat(upOffsetX)},${getPreFloat(upOffsetY)}）"
                setTitle(title)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("重置")
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.title == "重置") {
            xy_view.reset()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * 保留一位小数
     */
    private fun getPreFloat(float: Float): Float {
        val dc = BigDecimal(float.toDouble())
        return dc.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
    }
}