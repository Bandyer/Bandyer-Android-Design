package com.kaleyra.collaboration_suite_phone_ui.extensions

import android.util.Rational

object RationalExtensions {

    private val MIN_PIP_RATIONAL = Rational(9, 21)

    private val MAX_PIP_RATIONAL = Rational(22, 9)

    fun coerceRationalForPip(rational: Rational) = rational.coerceIn(MIN_PIP_RATIONAL, MAX_PIP_RATIONAL)
}