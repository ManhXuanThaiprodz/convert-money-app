package com.example.convertmoney

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Danh sách các loại tiền tệ
    private val currencyList = listOf("USD", "VND", "EUR", "JPY", "KRW")

    // Map: Mã tiền tệ -> Biểu tượng
    private val currencySymbols = mapOf(
        "USD" to "$",
        "VND" to "đ",
        "EUR" to "€",
        "JPY" to "¥",
        "KRW" to "₩"
    )

    // Tỷ giá cố định: 1 đơn vị -> USD
    private val currentRatetoUSD = mapOf(
        "USD" to 1.0,
        "VND" to (1 / 23000.0), // 1 VND ~ 0.0000435 USD
        "EUR" to 1.1,          // 1 EUR ~ 1.1 USD
        "JPY" to 0.008,        // 1 JPY ~ 0.008 USD
        "KRW" to 0.00078       // 1 KRW ~ 0.00078 USD
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Ánh xạ các View
        val tvSymbolFrom = findViewById<TextView>(R.id.tvSymbolFrom)
        val etAmountFrom = findViewById<EditText>(R.id.etAmountFrom)
        val spinnerFrom = findViewById<Spinner>(R.id.spinnerFromCurrency)

        val tvSymbolTo = findViewById<TextView>(R.id.tvSymbolTo)
        val tvAmountTo = findViewById<TextView>(R.id.tvAmountTo)
        val spinnerTo = findViewById<Spinner>(R.id.spinnerToCurrency)

        val tvExchangeRate = findViewById<TextView>(R.id.tvExchangeRate)
        val btnConvert = findViewById<Button>(R.id.btnConvert)

        // 2. Tạo Adapter cho Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        // 3. Xử lý khi Spinner "From" thay đổi
        spinnerFrom.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencyList[position]
                // Cập nhật biểu tượng tiền tệ nguồn
                tvSymbolFrom.text = currencySymbols[selectedCurrency]

                // Gọi hàm cập nhật tỷ giá hiển thị
                updateExchangeRate(tvExchangeRate, spinnerFrom, spinnerTo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì
            }
        }

        // 4. Xử lý khi Spinner "To" thay đổi
        spinnerTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedCurrency = currencyList[position]
                // Cập nhật biểu tượng tiền tệ đích
                tvSymbolTo.text = currencySymbols[selectedCurrency]

                // Gọi hàm cập nhật tỷ giá hiển thị
                updateExchangeRate(tvExchangeRate, spinnerFrom, spinnerTo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Không làm gì
            }
        }

        // 5. Xử lý khi bấm nút "Chuyển đổi"
        btnConvert.setOnClickListener {
            val amountString = etAmountFrom.text.toString().trim()

            // Kiểm tra người dùng có nhập số tiền hay chưa
            if (amountString.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Parse sang Double
            val amount = amountString.toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lấy loại tiền nguồn và đích
            val fromCurrency = spinnerFrom.selectedItem.toString()
            val toCurrency = spinnerTo.selectedItem.toString()

            // Tính toán chuyển đổi
            val result = convertCurrency(amount, fromCurrency, toCurrency)

            // Hiển thị kết quả lên TextView
            tvAmountTo.text = String.format("%.4f", result)

            // Cập nhật tỷ giá (nếu muốn) - thường đã cập nhật khi chọn Spinner
            updateExchangeRate(tvExchangeRate, spinnerFrom, spinnerTo)
        }
    }

    /**
     * Hàm chuyển đổi tiền tệ từ fromCurrency sang toCurrency
     * Logic: (amount * rateFromUSD) / rateToUSD
     * (vì currentRatetoUSD[fromCurrency] là tỉ giá từ 1 đơn vị fromCurrency -> USD)
     */
    private fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val rateFromUSD = currentRatetoUSD[fromCurrency] ?: 1.0
        val rateToUSD = currentRatetoUSD[toCurrency] ?: 1.0

        // amount -> USD -> toCurrency
        return amount * (rateFromUSD / rateToUSD)
    }

    /**
     * Cập nhật TextView hiển thị tỷ giá
     * Ví dụ: "1 USD = 23185.00 VND"
     */
    private fun updateExchangeRate(tv: TextView, spinnerFrom: Spinner, spinnerTo: Spinner) {
        val fromCurrency = spinnerFrom.selectedItem.toString()
        val toCurrency = spinnerTo.selectedItem.toString()

        // Nếu 2 loại tiền trùng nhau
        if (fromCurrency == toCurrency) {
            tv.text = "1 $fromCurrency = 1 $toCurrency"
            return
        }

        val fromRate = currentRatetoUSD[fromCurrency] ?: 1.0
        val toRate = currentRatetoUSD[toCurrency] ?: 1.0
        val rate = fromRate / toRate

        tv.text = "1 $fromCurrency = ${String.format("%.4f", rate)} $toCurrency"
    }
}
