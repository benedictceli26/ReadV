package com.mnhs.readventures.data

/**
 * Sample reading materials + quizzes, created by us for the demo.
 *
 * 5 materials, 5 multiple-choice questions each (4 choices, 1 correct).
 * Grade 7 level, short paragraphs. These are the fallback when Firestore
 * is not configured, and the seed data to upload to Firestore later.
 */
object SampleData {

    val levels: List<Level> = listOf(
        Level(
            id = "level-1",
            name = "Level 1: Beginner",
            description = "Grade 7 reading foundation with short, inspiring stories.",
            order = 1
        ),
        Level(
            id = "level-2",
            name = "Level 2: Intermediate",
            description = "Explore science and real-world environmental articles.",
            order = 2
        ),
        Level(
            id = "level-3",
            name = "Level 3: Advanced",
            description = "Classic literature with deeper comprehension questions.",
            order = 3
        )
    )

    val materials: List<ReadingMaterial> = listOf(
        ReadingMaterial(
            id = "power-of-kindness",
            title = "The Power of Kindness",
            level = "Level 1 (Grade 7)",
            topic = "Life Lessons",
            category = "Fiction",
            kind = "Stories",
            body = "One morning, Ana saw an old man struggling to carry a heavy basket of fruits at the market. Most people walked past him, busy with their own errands. Ana stopped, smiled, and offered to help. Together, they carried the basket to the old man's small store.\n\nThe old man thanked Ana and gave her a ripe mango as a reward. Ana did not expect anything in return, but the man's smile made her day brighter. She realized that a small act of kindness can change someone's whole morning.\n\nAt school, Ana shared the story with her classmates. Soon, her friends began helping others too — holding doors, sharing food, and cheering up sad classmates. Kindness spread from one person to another, just like a ripple in a pond.\n\nAna learned that kindness does not need to be big or expensive. A smile, a helping hand, or a kind word is enough. When we are kind, we make the world around us a better place for everyone.",
            createdBy = "sample",
            isTeacherMade = false,
            levelId = "level-1"
        ),
        ReadingMaterial(
            id = "kind-farmer",
            title = "The Kind Farmer",
            level = "Level 1 (Grade 7)",
            topic = "Short Story",
            category = "Fiction",
            kind = "Stories",
            body = "Mang Jose was a farmer who lived in a small village in Muntinlupa. Every morning, he worked in his field, planting rice and vegetables. He was not rich, but he was known as the kindest man in the village.\n\nOne dry season, the crops of his neighbors began to wilt. Mang Jose shared his water and his seeds with them, even though his own harvest would be smaller. \"We are neighbors,\" he said. \"When one of us is hungry, all of us should help.\"\n\nWhen the rains finally came, the whole village had a good harvest. The neighbors thanked Mang Jose and shared their food with him. They said his kindness had saved the village.\n\nMang Jose smiled and said, \"A kind heart is the best harvest of all.\" From that day on, the villagers always helped one another, just as Mang Jose had taught them.",
            createdBy = "sample",
            isTeacherMade = false,
            levelId = "level-1"
        ),
        ReadingMaterial(
            id = "volcanoes",
            title = "Volcanoes",
            level = "Level 2 (Intermediate)",
            topic = "Science",
            category = "Science",
            kind = "Informational",
            body = "A volcano is an opening in the Earth's surface where hot rock, ash, and gas can escape. Deep inside the Earth, rock is so hot that it melts into a thick liquid called magma. When pressure builds up, magma pushes upward and erupts through the volcano.\n\nThe Philippines is home to many volcanoes, including Mayon Volcano in Albay and Taal Volcano in Batangas. Mayon is famous for its almost perfect cone shape. Taal is one of the smallest active volcanoes in the world, and it sits on an island inside a lake.\n\nVolcanic eruptions can be dangerous. They can send out ash, lava, and poisonous gases. That is why scientists from PHIVOLCS watch volcanoes closely and warn people when an eruption might happen.\n\nBut volcanoes also help the land. Volcanic ash makes the soil rich and good for farming. Hot springs near volcanoes are used for bathing and for making electricity. Volcanoes remind us that the Earth is alive and always changing.",
            createdBy = "sample",
            isTeacherMade = false,
            levelId = "level-2"
        ),
        ReadingMaterial(
            id = "caring-environment",
            title = "Caring for Our Environment",
            level = "Level 2 (Intermediate)",
            topic = "Environment",
            category = "Non-Fiction",
            kind = "Informational",
            body = "Our environment is the world around us — the air we breathe, the water we drink, the trees, and the animals. When we take care of the environment, we take care of ourselves and the future.\n\nOne simple way to help is to reduce waste. We can bring our own water bottles and shopping bags instead of using plastic ones. We can also separate our trash so that paper, plastic, and bottles can be recycled.\n\nPlanting trees is another way to help. Trees clean the air, give shade, and provide homes for birds. In Muntinlupa, many schools and barangays hold tree-planting activities every year.\n\nEveryone can do something small each day. Saving water, turning off lights, and not throwing trash in rivers are small actions that add up. If we all work together, we can keep our city clean and green for the next generation.",
            createdBy = "sample",
            isTeacherMade = false,
            levelId = "level-2"
        ),
        ReadingMaterial(
            id = "honest-woodcutter",
            title = "The Honest Woodcutter",
            level = "Level 3 (Advanced)",
            topic = "Classic",
            category = "Classic",
            kind = "Stories",
            body = "Long ago, a poor woodcutter worked near a deep river. One day, his axe slipped from his hand and fell into the water. He had no money to buy a new axe, so he sat by the river and cried.\n\nSuddenly, a spirit appeared from the river, holding a golden axe. \"Is this your axe?\" the spirit asked. \"No,\" said the woodcutter. \"Mine was only made of iron.\" Then the spirit showed a silver axe. Again, the woodcutter shook his head.\n\nFinally, the spirit brought out the woodcutter's old iron axe. \"Yes, that is mine!\" the woodcutter cried happily. The spirit was pleased with his honesty and gave him all three axes as a reward.\n\nThe woodcutter went home rich, but he remained honest and hardworking. His story teaches us that honesty is always rewarded, even when no one is watching.",
            createdBy = "sample",
            isTeacherMade = false,
            levelId = "level-3"
        )
    )

    val questions: List<Question> = listOf(
        // --- The Power of Kindness (5) ---
        Question("pk-q1", "power-of-kindness", "What did Ana do when she saw the old man at the market?", listOf("She walked past him", "She offered to help carry the basket", "She bought all his fruits", "She called the police"), 1),
        Question("pk-q2", "power-of-kindness", "What did the old man give Ana as a reward?", listOf("A bag of rice", "Some money", "A ripe mango", "A new basket"), 2),
        Question("pk-q3", "power-of-kindness", "What happened after Ana shared her story at school?", listOf("Her classmates laughed at her", "Her friends began helping others too", "The teacher punished her", "Nobody listened to her"), 1),
        Question("pk-q4", "power-of-kindness", "According to the story, what is kindness compared to?", listOf("A ripple in a pond", "A heavy basket", "A ripe mango", "A bright morning"), 0),
        Question("pk-q5", "power-of-kindness", "What is the main lesson of the story?", listOf("Kindness must be big and expensive", "Only rich people can be kind", "Small acts of kindness can make the world better", "We should expect a reward for helping"), 2),
        // --- The Kind Farmer (5) ---
        Question("kf-q1", "kind-farmer", "What did Mang Jose do for a living?", listOf("He was a teacher", "He was a farmer", "He was a fisherman", "He was a shopkeeper"), 1),
        Question("kf-q2", "kind-farmer", "Why did Mang Jose share his water and seeds?", listOf("He had too much water", "He wanted to be paid", "He believed neighbors should help one another", "The village forced him to"), 2),
        Question("kf-q3", "kind-farmer", "What happened when the rains finally came?", listOf("The crops all died", "The whole village had a good harvest", "Mang Jose lost his farm", "The river dried up"), 1),
        Question("kf-q4", "kind-farmer", "What did the neighbors do to thank Mang Jose?", listOf("They gave him a new house", "They shared their food with him", "They made him village chief", "They gave him money"), 1),
        Question("kf-q5", "kind-farmer", "What did Mang Jose say is the best harvest of all?", listOf("A big harvest of rice", "A kind heart", "A new field", "A lot of money"), 1),
        // --- Volcanoes (5) ---
        Question("vo-q1", "volcanoes", "What is magma?", listOf("Cold rock under the sea", "Hot melted rock deep inside the Earth", "A type of volcanic ash", "A gas from a volcano"), 1),
        Question("vo-q2", "volcanoes", "Which volcano is famous for its almost perfect cone shape?", listOf("Taal Volcano", "Mayon Volcano", "Mount Apo", "Mount Pinatubo"), 1),
        Question("vo-q3", "volcanoes", "Where is Taal Volcano located?", listOf("On a mountain top in Albay", "On an island inside a lake in Batangas", "Under the sea near Cebu", "In the middle of Manila"), 1),
        Question("vo-q4", "volcanoes", "Who watches volcanoes in the Philippines and warns people?", listOf("PHIVOLCS", "PAGASA", "DepEd", "DOH"), 0),
        Question("vo-q5", "volcanoes", "How do volcanoes help the land?", listOf("They make the soil rich and good for farming", "They stop all rain", "They make the land flat", "They remove all trees"), 0),
        // --- The Honest Woodcutter (5) ---
        Question("hw-q1", "honest-woodcutter", "What happened to the woodcutter's axe?", listOf("It broke in half", "It was stolen", "It fell into the river", "He sold it"), 2),
        Question("hw-q2", "honest-woodcutter", "What did the spirit show first?", listOf("An iron axe", "A silver axe", "A golden axe", "A wooden axe"), 2),
        Question("hw-q3", "honest-woodcutter", "Why did the woodcutter refuse the golden axe?", listOf("He did not like gold", "It was too heavy", "It was not his axe", "He was afraid of the spirit"), 2),
        Question("hw-q4", "honest-woodcutter", "What reward did the spirit give the woodcutter?", listOf("Only his old axe", "All three axes", "A bag of gold coins", "A new house"), 1),
        Question("hw-q5", "honest-woodcutter", "What is the main lesson of the story?", listOf("Honesty is always rewarded", "Gold is the most important thing", "Spirits live in every river", "Poor people should not work"), 0),
        // --- Caring for Our Environment (5) ---
        Question("ce-q1", "caring-environment", "Which of these is a way to reduce waste?", listOf("Using a new plastic bag every day", "Bringing your own water bottle and shopping bag", "Throwing trash in the river", "Burning all your garbage"), 1),
        Question("ce-q2", "caring-environment", "Why should we separate our trash?", listOf("So it looks neat", "So paper, plastic, and bottles can be recycled", "So it is heavier", "So animals can eat it"), 1),
        Question("ce-q3", "caring-environment", "What do trees do for us?", listOf("They clean the air and give shade", "They use up all the water", "They block the sunlight completely", "They make the soil poor"), 0),
        Question("ce-q4", "caring-environment", "Which small action helps the environment?", listOf("Leaving lights on all day", "Wasting water", "Turning off lights when not in use", "Throwing trash anywhere"), 2),
        Question("ce-q5", "caring-environment", "What is the main idea of the text?", listOf("Only the government can save the environment", "Small actions by everyone can keep our city clean and green", "Trees are not important", "Plastic is good for the environment"), 1)
    )

    fun questionsFor(materialId: String): List<Question> =
        questions.filter { it.materialId == materialId }

    val dictionaryWords: List<DictionaryWord> = listOf(
        // The Power of Kindness (Level 1)
        DictionaryWord("struggling", "having difficulty doing something or putting in strong effort", "Ana saw an old man struggling to carry a heavy basket.", "verb", "power-of-kindness", "Level 1 (Grade 7)"),
        DictionaryWord("errands", "short trips taken to do small jobs, chores, or tasks", "Most people walked past him, busy with their own errands.", "noun", "power-of-kindness", "Level 1 (Grade 7)"),
        DictionaryWord("reward", "something given or received in return for good work or behavior", "The old man gave her a ripe mango as a reward.", "noun", "power-of-kindness", "Level 1 (Grade 7)"),
        DictionaryWord("ripple", "a small wave or gradual spread of an effect like ripples in water", "Kindness spread from one person to another, just like a ripple in a pond.", "noun", "power-of-kindness", "Level 1 (Grade 7)"),

        // The Kind Farmer (Level 1)
        DictionaryWord("wilt", "to become limp, droop, or bend down due to lack of water or heat", "The crops of his neighbors began to wilt during the dry season.", "verb", "kind-farmer", "Level 1 (Grade 7)"),
        DictionaryWord("harvest", "the gathering of crops or the amount of crops gathered in a season", "When the rains came, the whole village had a good harvest.", "noun", "kind-farmer", "Level 1 (Grade 7)"),
        DictionaryWord("villagers", "people who live in a small town or rural village community", "From that day on, the villagers always helped one another.", "noun", "kind-farmer", "Level 1 (Grade 7)"),
        DictionaryWord("neighbors", "people living near one another in the same community", "When one of us is hungry, all of us should help our neighbors.", "noun", "kind-farmer", "Level 1 (Grade 7)"),

        // Volcanoes (Level 2)
        DictionaryWord("magma", "hot melted liquid rock located deep beneath the Earth's crust", "Deep inside the Earth, rock melts into a thick liquid called magma.", "noun", "volcanoes", "Level 2 (Intermediate)"),
        DictionaryWord("erupts", "explodes or bursts out suddenly with rock, ash, and lava", "When pressure builds up, magma pushes upward and erupts through the volcano.", "verb", "volcanoes", "Level 2 (Intermediate)"),
        DictionaryWord("poisonous", "containing harmful chemicals that can cause sickness or death", "Volcanic eruptions can send out ash, lava, and poisonous gases.", "adjective", "volcanoes", "Level 2 (Intermediate)"),
        DictionaryWord("active", "capable of erupting or showing volcanic activity recently", "Taal is one of the smallest active volcanoes in the world.", "adjective", "volcanoes", "Level 2 (Intermediate)"),

        // Caring for Our Environment (Level 2)
        DictionaryWord("environment", "the natural surroundings including air, water, land, plants, and animals", "Our environment is the world around us that we must protect.", "noun", "caring-environment", "Level 2 (Intermediate)"),
        DictionaryWord("recycled", "processed and converted into new items so materials can be reused", "Bottles and paper can be recycled instead of thrown away.", "verb", "caring-environment", "Level 2 (Intermediate)"),
        DictionaryWord("generation", "all the people born and living at about the same time period", "We can keep our city clean and green for the next generation.", "noun", "caring-environment", "Level 2 (Intermediate)"),
        DictionaryWord("activities", "actions or events organized for a specific goal or community effort", "Schools and barangays hold tree-planting activities every year.", "noun", "caring-environment", "Level 2 (Intermediate)"),

        // The Honest Woodcutter (Level 3)
        DictionaryWord("spirit", "a supernatural or magical being living in nature", "Suddenly, a spirit appeared from the river, holding a golden axe.", "noun", "honest-woodcutter", "Level 3 (Advanced)"),
        DictionaryWord("honesty", "the quality of always telling the truth and being fair", "The spirit was pleased with his honesty and rewarded him.", "noun", "honest-woodcutter", "Level 3 (Advanced)"),
        DictionaryWord("rewarded", "given a prize, money, or praise for good or truthful actions", "His story teaches us that honesty is always rewarded.", "verb", "honest-woodcutter", "Level 3 (Advanced)"),
        DictionaryWord("hardworking", "putting continuous effort, dedication, and energy into tasks", "The woodcutter went home rich, but he remained honest and hardworking.", "adjective", "honest-woodcutter", "Level 3 (Advanced)")
    )

}
