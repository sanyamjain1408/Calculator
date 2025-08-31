package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var display: EditText
    private var lastNumeric: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.Text)

        // Digits 0-9
        val digits = listOf(
            R.id.zero, R.id.one, R.id.two, R.id.three,
            R.id.four, R.id.five, R.id.six,
            R.id.seven, R.id.eight, R.id.nine
        )

        for (id in digits) {
            findViewById<Button>(id).setOnClickListener {
                display.append((it as Button).text)
                lastNumeric = true
            }
        }

        // Operators (+, -, ×, ÷, %)
        val operators = listOf(R.id.add, R.id.sub, R.id.multi, R.id.divide, R.id.percent)

        for (id in operators) {
            findViewById<Button>(id).setOnClickListener {
                if (lastNumeric) {
                    display.append((it as Button).text)
                    lastNumeric = false
                    lastDot = false
                }
            }
        }

        // Decimal point
        findViewById<Button>(R.id.dout).setOnClickListener {
            if (lastNumeric && !lastDot) {
                display.append(".")
                lastNumeric = false
                lastDot = true
            }
        }

        // Clear (AC)
        findViewById<Button>(R.id.ac).setOnClickListener {
            display.setText("")
            lastNumeric = false
            lastDot = false
        }

        // Backspace
        findViewById<Button>(R.id.back).setOnClickListener {
            val current = display.text.toString()
            if (current.isNotEmpty()) {
                display.setText(current.dropLast(1))
            }
        }

        // Equal (=)
        findViewById<Button>(R.id.equal).setOnClickListener {
            try {
                val result = eval(display.text.toString())
                display.setText(result.toString())
            } catch (e: Exception) {
                display.setText("Error")
            }
        }
    }

    // Simple expression evaluator (handles +, -, *, /, %)
    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + expression[pos])
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor | term `%` factor
            // factor = `+` factor | `-` factor | number | `(` expression `)`

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('x'.code) || eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        eat('%'.code) -> x % parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) { // numbers
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: ${ch.toChar()}")
                }
                return x
            }
        }.parse()
    }
}
