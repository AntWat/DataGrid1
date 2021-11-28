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
 * A Fragment to display a test data table
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

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _context = getActivity()?.getApplicationContext()

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        // Display the table
        val allCells = displayTestTable(inflater)

        val content: View = _binding!!.mainArea
        content.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                //Remove the observer so we don't get this callback for EVERY layout pass
                content.viewTreeObserver.removeGlobalOnLayoutListener(this)

                //Resize the columns to match the maximum width
                setColumnWidths(allCells, fun (v: View, w: Int) {
                    val tv = v.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
                    var lp  = LayoutParams(w, LayoutParams.WRAP_CONTENT)
                    v.layoutParams = lp

                    tv.setGravity(Gravity.CENTER)
                })
            }
        })

        return binding.root

    }

    fun getTestData(numCols:Int, numRows:Int) : SimpleTable
    {
        val table = SimpleTable()
        val longHdrCol = 2
        val longDataCol = 1
        val numLongRows = 4
        val warning_row = 6


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
                var datVal: String = (if ((r < numLongRows) && (c == longDataCol)) "Longer Val${r},${c+1}" else "Val${r},${c+1}")
                if  (r==warning_row) { datVal = "War${r},${c+1}" }
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
        var allCells = Array(dataTable.NumRows+1) {Array<View?>(dataTable.NumColumns) {null} }

        // ------------- Main header (fixed vertically)
        val wrapWrapTableRowParams: TableRow.LayoutParams =
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        var row = TableRow(_context)
        val header = _binding?.tableHeader as TableLayout
        row.setLayoutParams(wrapWrapTableRowParams)
        row.setGravity(Gravity.CENTER)

        for (c in 0..dataTable.NumColumns-1) {
            val colHdrText = dataTable.Headers[c]
            allCells[0][c] = createHeaderCellFromTemplate(inflater, colHdrText)
            setHeaderBg(allCells[0][c] as View)
            row.addView(allCells[0][c])
        }
        header.addView(row)

        // ------------- Row header (fixed horizontally)
        val fixedColumn = _binding?.fixedColumn as TableLayout

        // ------------- Rest of the table (within a scroll view)
        val scrollablePart = _binding?.scrollablePart as TableLayout

        // ------------- Create the rows
        for (r in 0..dataTable.NumRows-1) {

            // ----------- Create RowHeader
            val rowHdrText = (if (r == -1) dataTable.Headers[0] else dataTable.Rows[r][0])
            allCells[r+1][0] = createHeaderCellFromTemplate(inflater, rowHdrText)
            setHeaderBg(allCells[r+1][0] as View)
            row = TableRow(_context)
            row.addView(allCells[r+1][0])
            fixedColumn.addView(row)

            // ----------- Create Row Data
            row = TableRow(_context)
            for (c in 1..dataTable.NumColumns-1) {
                val dataVal = (if (r == -1) dataTable.Headers[c] else dataTable.Rows[r][c])
                allCells[r+1][c] = createDataCellFromTemplate(inflater, dataVal)
                setContentBg(allCells[r+1][c] as View)
                row.addView(allCells[r+1][c])
            }

            row.setLayoutParams(wrapWrapTableRowParams)
            row.setGravity(Gravity.CENTER)
            scrollablePart.addView(row)
        }

        return  allCells
    }

    fun setColumnWidths(allCells: Array<Array<View?>>, setItemWidth: (v: View, w: Int) -> Unit)
    {
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
                setItemWidth(vw, colWidths[c])
            }
        }
    }

    fun createHeaderCellFromTemplate(inflater: LayoutInflater, text: String?): View {
        val cellView: View = inflater.inflate(com.ant_waters.datagrid1.R.layout.header_cell, null)
        val tv = cellView.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
        tv.text = text

        return cellView
    }

    fun createDataCellFromTemplate(inflater: LayoutInflater, text: String?): View {
        var templateId = com.ant_waters.datagrid1.R.layout.data_cell
        if (text?.startsWith("War")!!) { templateId = com.ant_waters.datagrid1.R.layout.warning_data_cell }
        val cellView: View = inflater.inflate(templateId, null)

        val tv = cellView.findViewById<View>(com.ant_waters.datagrid1.R.id.cell_text_view) as TextView
        tv.text = text

        return cellView
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