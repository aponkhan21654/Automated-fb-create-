package com.example.automation

import kotlin.random.Random

data class GeneratedProfile(
    val firstName: String,
    val lastName: String,
    val emailOrPhone: String,
    val password: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val gender: String // "Male", "Female"
) {
    val fullName: String get() = "$firstName $lastName"
    val dobFormatted: String get() = String.format("%02d/%02d/%04d", birthDay, birthMonth, birthYear)
}

object ProfileGenerator {

    private val bangladeshiMaleFirstNames = listOf(
        "Tanvir", "Sabbir", "Rahim", "Shakib", "Naim", "Abrar", "Fahim", "Mahmud", "Imran",
        "Nayeem", "Tamim", "Jubayer", "Rakib", "Arif", "Hasan", "Mehedi", "Rayhan", "Siam", "Tawhid"
    )

    private val bangladeshiFemaleFirstNames = listOf(
        "Ayesha", "Nusrat", "Sadia", "Fatema", "Mithila", "Tazrin", "Mim", "Sharmin",
        "Rumana", "Sabrina", "Farhana", "Jannat", "Priya", "Noshin", "Lamia", "Afrin", "Sumaiya"
    )

    private val bangladeshiLastNames = listOf(
        "Hasan", "Ahmed", "Hossain", "Chowdhury", "Rahman", "Islam", "Khan", "Siddique", "Ali", "Alam"
    )

    private val globalMaleFirstNames = listOf(
        "Alex", "David", "Michael", "Ethan", "James", "Daniel", "Matthew", "Oliver", "Lucas", "Noah"
    )

    private val globalFemaleFirstNames = listOf(
        "Emma", "Sophia", "Olivia", "Ava", "Isabella", "Mia", "Emily", "Chloe", "Grace", "Lily"
    )

    private val globalLastNames = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Wilson"
    )

    private val emailDomains = listOf(
        "gmail.com", "outlook.com", "yahoo.com", "hotmail.com", "icloud.com", "proton.me", "tempmail.org"
    )

    enum class PresetType { BANGLADESHI, GLOBAL, MIXED }
    enum class PasswordMode { RANDOM_STRONG, CUSTOM_FIXED, PREFIX_RANDOM }

    fun generateRandomProfile(): GeneratedProfile = generate()

    fun generate(
        preset: PresetType = PresetType.BANGLADESHI,
        forcedGender: String? = null,
        useEmail: Boolean = true,
        passwordMode: PasswordMode = PasswordMode.RANDOM_STRONG,
        customPassword: String = "Pass#2026",
        customPrefix: String = "FbUser"
    ): GeneratedProfile {
        val isMale = forcedGender?.lowercase()?.contains("male") ?: Random.nextBoolean()
        val genderStr = if (isMale) "Male" else "Female"

        val firstName = when (preset) {
            PresetType.BANGLADESHI -> if (isMale) bangladeshiMaleFirstNames.random() else bangladeshiFemaleFirstNames.random()
            PresetType.GLOBAL -> if (isMale) globalMaleFirstNames.random() else globalFemaleFirstNames.random()
            PresetType.MIXED -> if (Random.nextBoolean()) {
                if (isMale) bangladeshiMaleFirstNames.random() else bangladeshiFemaleFirstNames.random()
            } else {
                if (isMale) globalMaleFirstNames.random() else globalFemaleFirstNames.random()
            }
        }

        val lastName = when (preset) {
            PresetType.BANGLADESHI -> bangladeshiLastNames.random()
            PresetType.GLOBAL -> globalLastNames.random()
            PresetType.MIXED -> if (Random.nextBoolean()) bangladeshiLastNames.random() else globalLastNames.random()
        }

        val randomYear = Random.nextInt(1992, 2004)
        val randomMonth = Random.nextInt(1, 13)
        val randomDay = Random.nextInt(1, 28)

        val cleanFirstName = firstName.lowercase().replace(" ", "")
        val cleanLastName = lastName.lowercase().replace(" ", "")
        val randomDigits = Random.nextInt(100, 9999)

        val emailOrPhone = if (useEmail) {
            "$cleanFirstName.$cleanLastName$randomDigits@${emailDomains.random()}"
        } else {
            val prefixes = listOf("017", "018", "019", "016", "013", "015")
            "${prefixes.random()}${String.format("%08d", Random.nextInt(10000000, 99999999))}"
        }

        val password = when (passwordMode) {
            PasswordMode.RANDOM_STRONG -> "Fb${cleanFirstName.replaceFirstChar { it.uppercase() }}#${Random.nextInt(100, 999)}"
            PasswordMode.CUSTOM_FIXED -> customPassword.ifBlank { "Pass#2026" }
            PasswordMode.PREFIX_RANDOM -> "${customPrefix.ifBlank { "FbUser" }}#${Random.nextInt(10000, 99999)}"
        }

        return GeneratedProfile(
            firstName = firstName,
            lastName = lastName,
            emailOrPhone = emailOrPhone,
            password = password,
            birthDay = randomDay,
            birthMonth = randomMonth,
            birthYear = randomYear,
            gender = genderStr
        )
    }
}
