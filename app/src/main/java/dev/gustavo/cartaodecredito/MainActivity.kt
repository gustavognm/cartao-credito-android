package dev.gustavo.cartaodecredito

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var etCardNumber: EditText
    private lateinit var etCardHolder: EditText
    private lateinit var etExpiry: EditText
    private lateinit var etCvv: EditText
    private lateinit var btnConfirmar: Button

    private lateinit var tvCardNumberPreview: TextView
    private lateinit var tvCardHolderPreview: TextView
    private lateinit var tvCardExpiryPreview: TextView
    private lateinit var tvCvvPreview: TextView
    private lateinit var tvBrandLogo: TextView

    private lateinit var cardFront: CardView
    private lateinit var cardBack: CardView

    private var atualizandoCardNumber = false
    private var atualizandoExpiry = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val extraPadding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + extraPadding,
                systemBars.top + extraPadding,
                systemBars.right + extraPadding,
                systemBars.bottom + extraPadding
            )
            insets
        }

        etCardNumber = findViewById(R.id.etCardNumber)
        etCardHolder = findViewById(R.id.etCardHolder)
        etExpiry = findViewById(R.id.etExpiry)
        etCvv = findViewById(R.id.etCvv)
        btnConfirmar = findViewById(R.id.btnConfirmar)

        tvCardNumberPreview = findViewById(R.id.tvCardNumberPreview)
        tvCardHolderPreview = findViewById(R.id.tvCardHolderPreview)
        tvCardExpiryPreview = findViewById(R.id.tvCardExpiryPreview)
        tvCvvPreview = findViewById(R.id.tvCvvPreview)
        tvBrandLogo = findViewById(R.id.tvBrandLogo)

        cardFront = findViewById(R.id.cardFront)
        cardBack = findViewById(R.id.cardBack)

        configurarMascaraCardNumber()
        configurarPreviewCardHolder()
        configurarMascaraExpiry()
        configurarPreviewCvv()
        configurarFlipDoCartao()
        configurarBotaoConfirmar()
    }

    // Máscara do número do cartão: espaço a cada 4 dígitos, máximo 16 dígitos
    private fun configurarMascaraCardNumber() {
        etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (atualizandoCardNumber) return
                atualizandoCardNumber = true

                val digitos = s.toString().filter { it.isDigit() }.take(16)
                val textoFormatado = digitos.chunked(4).joinToString(" ")
                etCardNumber.setText(textoFormatado)
                etCardNumber.setSelection(textoFormatado.length)

                tvCardNumberPreview.text = textoFormatado.ifEmpty {
                    getString(R.string.card_number_placeholder)
                }

                atualizarBandeira(digitos)

                atualizandoCardNumber = false
            }
        })
    }

    // Desafio 2: identifica a bandeira pelos primeiros dígitos e atualiza logo + cor do cartão
    private fun atualizarBandeira(digitos: String) {
        val prefixo2 = digitos.take(2).toIntOrNull()
        val prefixo4 = digitos.take(4).toIntOrNull()

        val (nomeBandeira, cor) = when {
            digitos.isEmpty() -> null to Color.parseColor("#3F51B5")
            digitos.startsWith("4") -> getString(R.string.brand_visa) to Color.parseColor("#1A1F71")
            prefixo2 != null && prefixo2 in 51..55 -> getString(R.string.brand_mastercard) to Color.parseColor("#EB001B")
            prefixo4 != null && prefixo4 in 2221..2720 -> getString(R.string.brand_mastercard) to Color.parseColor("#EB001B")
            else -> getString(R.string.brand_other) to Color.parseColor("#424242")
        }

        tvBrandLogo.text = nomeBandeira ?: ""
        cardFront.setCardBackgroundColor(cor)
        cardBack.setCardBackgroundColor(cor)
    }

    // Máscara da validade: formato MM/AA
    private fun configurarMascaraExpiry() {
        etExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (atualizandoExpiry) return
                atualizandoExpiry = true

                val textoFormatado = aplicarMascaraExpiry(s.toString())
                etExpiry.setText(textoFormatado)
                etExpiry.setSelection(textoFormatado.length)

                tvCardExpiryPreview.text = textoFormatado.ifEmpty {
                    getString(R.string.card_expiry_placeholder)
                }

                atualizandoExpiry = false
            }
        })
    }

    private fun aplicarMascaraExpiry(valor: String): String {
        val digitos = valor.filter { it.isDigit() }.take(4)
        return if (digitos.length > 2) {
            "${digitos.substring(0, 2)}/${digitos.substring(2)}"
        } else {
            digitos
        }
    }

    // Preview do nome do titular, sempre em maiúsculas (como num cartão real)
    private fun configurarPreviewCardHolder() {
        etCardHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val nome = s.toString()
                tvCardHolderPreview.text = nome.ifEmpty {
                    getString(R.string.card_holder_placeholder)
                }.uppercase()
            }
        })
    }

    // Preview do CVV no verso do cartão
    private fun configurarPreviewCvv() {
        etCvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val cvv = s.toString()
                tvCvvPreview.text = cvv.ifEmpty {
                    getString(R.string.card_cvv_placeholder)
                }
            }
        })
    }

    // Desafio 1: gira o cartão pra mostrar o verso ao focar no CVV, e volta pra frente nos outros campos
    private fun configurarFlipDoCartao() {
        etCvv.setOnFocusChangeListener { _, temFoco ->
            if (temFoco) flipParaVerso()
        }

        val voltarParaFrenteAoFocar = View.OnFocusChangeListener { _, temFoco ->
            if (temFoco) flipParaFrente()
        }
        etCardNumber.onFocusChangeListener = voltarParaFrenteAoFocar
        etCardHolder.onFocusChangeListener = voltarParaFrenteAoFocar
        etExpiry.onFocusChangeListener = voltarParaFrenteAoFocar
    }

    private fun flipParaVerso() {
        if (cardBack.visibility == View.VISIBLE) return
        animarFlip(viewSaindo = cardFront, viewEntrando = cardBack)
    }

    private fun flipParaFrente() {
        if (cardFront.visibility == View.VISIBLE) return
        animarFlip(viewSaindo = cardBack, viewEntrando = cardFront)
    }

    private fun animarFlip(viewSaindo: View, viewEntrando: View) {
        val distanciaCamera = 8000 * resources.displayMetrics.density
        viewSaindo.cameraDistance = distanciaCamera
        viewEntrando.cameraDistance = distanciaCamera

        val animacaoSaida = ObjectAnimator.ofFloat(viewSaindo, "rotationY", 0f, 90f)
        val animacaoEntrada = ObjectAnimator.ofFloat(viewEntrando, "rotationY", -90f, 0f)
        animacaoSaida.duration = 200
        animacaoEntrada.duration = 200

        animacaoSaida.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                viewSaindo.visibility = View.INVISIBLE
                viewEntrando.visibility = View.VISIBLE
                animacaoEntrada.start()
            }
        })

        animacaoSaida.start()
    }

    // Desafio: validação antes de "processar" os dados
    private fun configurarBotaoConfirmar() {
        btnConfirmar.setOnClickListener {
            val numeroDigitos = etCardNumber.text.toString().filter { it.isDigit() }
            val nome = etCardHolder.text.toString().trim()
            val validade = etExpiry.text.toString()
            val cvv = etCvv.text.toString()

            when {
                numeroDigitos.length != 16 ->
                    Toast.makeText(this, getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show()

                nome.length < 3 ->
                    Toast.makeText(this, getString(R.string.error_invalid_name), Toast.LENGTH_SHORT).show()

                validade.length != 5 ->
                    Toast.makeText(this, getString(R.string.error_invalid_expiry), Toast.LENGTH_SHORT).show()

                cvv.length != 3 ->
                    Toast.makeText(this, getString(R.string.error_invalid_cvv), Toast.LENGTH_SHORT).show()

                else ->
                    Toast.makeText(this, getString(R.string.success_confirm), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
