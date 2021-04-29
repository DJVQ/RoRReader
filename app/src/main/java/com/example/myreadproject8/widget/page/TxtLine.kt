package com.example.myreadproject8.widget.page

//Txt行
class TxtLine {
//    var position: Int? = null;

    //可为空
    var charsData: List<TxtChar>? = null

    fun getLineData(): String {
        var linedata = ""//初始化行
        if (charsData == null) return linedata //charsData为空直接返回

        //不为空执行下面内容
        charsData?.let {
            if (it.isEmpty()) return linedata
            for (c in it) {
                linedata += c.chardata//逐步加上charData
            }
        }
        return linedata
    }

    override fun toString(): String {
        return "ShowLine [Linedata=" + getLineData() + "]"
    }

}