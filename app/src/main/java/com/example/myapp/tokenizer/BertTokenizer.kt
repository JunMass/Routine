package com.example.myapp.tokenizer

import android.content.Context
import java.text.Normalizer

class BertTokenizer(private val context: Context) {
    private val vocab: Map<String, Int>
    private val unkToken = "[UNK]"
    private val clsToken = "[CLS]"
    private val sepToken = "[SEP]"
    private val padToken = "[PAD]"
    private val maxSeqLength = 128

    init {
        vocab = loadVocab()
    }

    private fun loadVocab(): Map<String, Int> {
        val vocabMap = mutableMapOf<String, Int>()
        context.assets.open("vocab.txt").bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, token ->
                vocabMap[token] = index
            }
        }
        return vocabMap
    }

    private fun normalize(text: String): String {
        return Normalizer.normalize(text.lowercase(), Normalizer.Form.NFKC)
    }

    fun encode(text: String): Pair<IntArray, IntArray> {
        val tokens = mutableListOf(clsToken)
        tokens += tokenize(normalize(text))
        tokens += sepToken

        val inputIds = tokens.map { vocab[it] ?: vocab[unkToken]!! }.toMutableList()
        val attentionMask = MutableList(inputIds.size) { 1 }

        while (inputIds.size < maxSeqLength) {
            inputIds.add(vocab[padToken]!!)
            attentionMask.add(0)
        }
        if (inputIds.size > maxSeqLength) {
            inputIds.subList(maxSeqLength, inputIds.size).clear()
            attentionMask.subList(maxSeqLength, attentionMask.size).clear()
        }

        return Pair(inputIds.toIntArray(), attentionMask.toIntArray())
    }

    private fun tokenize(text: String): List<String> {
        val tokens = mutableListOf<String>()
        for (word in text.split(" ")) {
            tokens += wordPieceTokenize(word)
        }
        return tokens
    }

    private fun wordPieceTokenize(word: String): List<String> {
        val subTokens = mutableListOf<String>()
        val chars = word.toCharArray()
        var start = 0

        while (start < chars.size) {
            var end = chars.size
            var found = false
            var curSub: String

            while (end > start) {
                curSub = chars.slice(start until end).joinToString("")
                if (start > 0) curSub = "##$curSub"
                if (vocab.containsKey(curSub)) {
                    subTokens.add(curSub)
                    start = end
                    found = true
                    break
                }
                end--
            }

            if (!found) {
                subTokens.add(unkToken)
                start++ // advance 1 char to avoid infinite loop
            }
        }
        return subTokens
    }
}