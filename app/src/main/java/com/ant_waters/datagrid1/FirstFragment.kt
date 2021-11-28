package com.ant_waters.datagrid1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ant_waters.datagrid1.databinding.FragmentFirstBinding
import android.view.Gravity

import android.widget.TextView;
import android.content.Context
import android.graphics.Color

import android.widget.TableLayout
import android.widget.TableRow

import android.widget.TableRow.LayoutParams;
import android.graphics.drawable.Drawable
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.LinearLayout


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var _context: Context? = null

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).toInt()
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _context = getActivity()?.getApplicationContext()

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val allCells = displayTestTable(inflater)

        val content: View = _binding!!.mainArea
        content.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //Remove it here unless you want to get this callback for EVERY layout pass
                content.viewTreeObserver.removeGlobalOnLayoutListener(this)

                //Resize the columns to match the maximum width
                setColumnWidths(allCells, fun (v: View, w: Int) {
                    val tv = v.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
                    tv.setWidth(w)
                    tv.setGravity(Gravity.CENTER)
                })
            }
        })

        return binding.root

    }

    fun getTestData(numCols:Int, numRows:Int) : SimpleTable
    {
        val table = SimpleTable()
        val longHdrCol = 0
        val longDataCol = 1
        val numLongRows = 4


        val headers = mutableListOf<String>()
        for (c in 0..numCols-1)
        {
            val hdr: String = (if (c == longHdrCol) "Longer Col${c+1}" else "Col ${c+1}")
            headers.add(hdr);
        }
        table.addHeaders(headers.toList())

        val values = mutableListOf<String>()
        for (r in 0..numRows-1)
        {
            values.clear()
            for (c in 0..numCols-1)
            {
                val datVal: String = (if ((r < numLongRows) && (c == longDataCol)) "Longer Val${r},${c+1}" else "Val${r},${c+1}")
                values.add(datVal);
            }
            table.addRow(values.toList())
        }
        return table
    }

    fun displayTestTable(inflater: LayoutInflater): Array<Array<View?>>
    {
        val testData = getTestData(20, 100)
        return displayTable(inflater, testData)
    }

    fun displayTable(inflater: LayoutInflater, dataTable: SimpleTable): Array<Array<View?>>
    {
        var cellBackground = getCellBackground()
        var allCells = Array(dataTable.NumRows+1) {Array<View?>(dataTable.NumColumns) {null} }

        val wrapWrapTableRowParams: TableRow.LayoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        val fixedRowHeight = 150
        val fixedHeaderHeight = 150
        val colWidth = 300

        var row = TableRow(_context)

        // Main header (fixed vertically)
        val header = _binding?.tableHeader as TableLayout
        row.setLayoutParams(wrapWrapTableRowParams)
        row.setGravity(Gravity.CENTER)
        //row.setBackgroundColor(Color.LTGRAY)

        for (c in 0..dataTable.NumColumns-1) {
//            allCells[0][c] = createCell(dataTable.Headers[c], colWidth, fixedHeaderHeight, cellBackground)
            val colHdrText = dataTable.Headers[c]
            allCells[0][c] = createHeaderCellFromTemplate(inflater, colHdrText)
            setHeaderBg(allCells[0][c] as View)
            row.addView(allCells[0][c])
        }
        header.addView(row)

        // Row header (fixed horizontally)
        val fixedColumn = _binding?.fixedColumn as TableLayout
        //fixedColumn?.setBackgroundColor(Color.LTGRAY)

        // Rest of the table (within a scroll view)
        val scrollablePart = _binding?.scrollablePart as TableLayout
        for (r in 0..dataTable.NumRows-1) {

            // ----------- Create RowHeader
//            allCells[r+1][0] = createCell(
//                (if (r == -1) dataTable.Headers[0] else dataTable.Rows[r][0]),
//                colWidth, fixedRowHeight, cellBackground)
            val rowHdrText = (if (r == -1) dataTable.Headers[0] else dataTable.Rows[r][0])
            allCells[r+1][0] = createHeaderCellFromTemplate(inflater, rowHdrText)
            setHeaderBg(allCells[r+1][0] as View)
            //fixedView?.setBackgroundColor(Color.LTGRAY)
            row = TableRow(_context)
            row.addView(allCells[r+1][0])
            fixedColumn.addView(row)

            // ----------- Create Row Data
            row = TableRow(_context)
            for (c in 1..dataTable.NumColumns-1) {
                val dataVal = (if (r == -1) dataTable.Headers[c] else dataTable.Rows[r][c])
//                allCells[r+1][c] = createCell(dataVal, colWidth, fixedHeaderHeight, cellBackground)
                allCells[r+1][c] = createDataCellFromTemplate(inflater, dataVal)
                setContentBg(allCells[r+1][c] as View)
                row.addView(allCells[r+1][c])
            }

            row.setLayoutParams(wrapWrapTableRowParams)
            row.setGravity(Gravity.CENTER)      //Gravity.CENTER
            //row.setBackgroundColor(Color.WHITE)
            scrollablePart.addView(row)
        }

        return  allCells
    }

    fun setColumnWidths(allCells: Array<Array<View?>>, setItemWidth: (v: View, w: Int) -> Unit)
    {
        //var cellBackground = getCellBackground()
        val numColumns: Int = allCells[0].size
        val colWidths = Array<Int>(numColumns, {0})
        for (r in 0..allCells.size-1)
        {
            for (c in 0..numColumns-1)
            {
                val vw : View = allCells[r][c]!!
                if (vw.width > colWidths[c]) { colWidths[c] = vw.width}
            }
        }
        for (r in 0..allCells.size-1)
        {
            for (c in 0..numColumns-1)
            {
                val vw = allCells[r][c]!! as LinearLayout
//                if  ((r == 0) || (r==1))
//                {
                    setItemWidth(vw, colWidths[c])
//                }
            }
        }
    }

    fun getCellBackground(): Drawable
    {
//        var cellBackground = getActivity()?.getDrawable(com.ant_waters.datagrid1.R.drawable.cell_border)?.mutate()
//        val dcs = cellBackground as LayerDrawable
//        val gradientDrawable = dcs.findDrawableByLayerId(com.ant_waters.datagrid1.R.id.cell_border_rectangle) as GradientDrawable
//        gradientDrawable?. setStroke(2, Color.DKGRAY)        // Width is pixels. not dp

        var cellBackground = getActivity()?.getDrawable(com.ant_waters.datagrid1.R.drawable.cell_border)
        return cellBackground!!
    }


    fun createHeaderCellFromTemplate(inflater: LayoutInflater, text: String?): View {
        val cellView: View = inflater.inflate(com.ant_waters.datagrid1.R.layout.header_cell, null)
        val tv = cellView.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
        tv.text = text

        return cellView
    }

    fun createDataCellFromTemplate(inflater: LayoutInflater, text: String?): View {
        val cellView: View = inflater.inflate(com.ant_waters.datagrid1.R.layout.data_cell, null)
        val tv = cellView.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
        tv.text = text

        return cellView
    }

    fun createCell(
        text: String?,
        widthInPercentOfScreenWidth: Int,
        fixedHeightInPixels: Int,
        cellBackground: Drawable?
    ): TextView {

        //val screenWidth = resources.displayMetrics.widthPixels
        var txtView = TextView(_context)
        txtView!!.setText(text)
        txtView!!.setTextColor(Color.BLACK)
        txtView!!.setTextSize(20.0F)

        //recyclableTextView!!.setWidth(widthInPercentOfScreenWidth * screenWidth / 100)
        //txtView!!.setWidth(widthInPercentOfScreenWidth)

        val w = dpToPx(56)
        val h = dpToPx(50)
//        txtView!!.setWidth(w)
//        txtView!!.setHeight(h)

        var lp  = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        //var lp  = LayoutParams()
        val mgn = dpToPx(2)
        lp.setMargins(mgn,mgn,mgn,mgn);
        txtView.layoutParams = lp

        //txtView!!.setHeight(fixedHeightInPixels)
        txtView!!.setGravity(Gravity.CENTER_HORIZONTAL + Gravity.CENTER_VERTICAL)

        txtView!!.setPadding(15, 5, 15, 5)

        //txtView!!.background = cellBackground

        return txtView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun setHeaderBg(view: View) {
        view.setBackgroundResource(com.ant_waters.datagrid1.R.drawable.table_header_cell_bg)
    }

    private fun setContentBg(view: View) {
        view.setBackgroundResource(com.ant_waters.datagrid1.R.drawable.table_content_cell_bg)
    }
}


class SimpleTable() {
    val NumRows
        get() = Rows.count()

    val NumColumns
        get() = Headers.count()

    var Headers = mutableListOf<String>()
    var Rows = mutableListOf<List<String/*Value*/>>()

    fun addHeaders(items: List<String>)
    {
        Headers.addAll(items)
    }
    fun addRow(items: List<String>)
    {
        if (items.count() != Headers.count()) { throw Exception("Bad row") }
        Rows.add(items)
    }
}