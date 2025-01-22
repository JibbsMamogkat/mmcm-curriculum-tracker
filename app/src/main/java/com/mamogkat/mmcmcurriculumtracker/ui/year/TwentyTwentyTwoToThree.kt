package com.mamogkat.mmcmcurriculumtracker.ui.year

import com.mamogkat.mmcmcurriculumtracker.ui.screens.Curriculum


val curriculumData: Map<String, List<Pair<String, List<Curriculum>>>> = mapOf(
    "First Year" to listOf(
        "First Year - Term 1" to listOf(
            Curriculum("CHM031", "Chemistry for Engineers", 4.5, 0.0, 3.0, null, null),
            Curriculum("CHM031L", "Chemistry for Engineers (Laboratory)", 0.0, 4.5, 1.0, null, "CHM031"),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
            Curriculum("HUM021", "Logic and Critical Thinking", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH031", "Mathematics for Engineers", 4.5, 0.0, 3.0, null, null),
            Curriculum("SS022", "Readings in Philippine History", 4.5, 0.0, 3.0, null, null),
            Curriculum("NSTP010", "National Service Training Program 1", 3.0, 0.0, 3.0, null, null),
            Curriculum("VE021", "Life Coaching Series 1", 1.5, 0.0, 1.0, null, null)
        ),
        "First Year - Term 2" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH041", "Engineering Calculus 1", 6.0, 0.0, 4.0, "MATH031", null),
            Curriculum("SS021", "Understanding the Self", 4.5, 0.0, 3.0, null, null),
            Curriculum("NSTP011P", "National Service Training Program 2 (Paired)", 3.0, 0.0, 3.0, "NSTP010", null),
            Curriculum("VE022", "Life Coaching Series 2", 1.5, 0.0, 1.0, "VE021", null)
        ),
        "First Year - Term 3" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),

    ),
    "Second Year" to listOf(
        "Second Year - Term 1" to listOf(
            Curriculum("CPE141L", "Programming Logic and Design (Lab)", 0.0, 9.0, 2.0, null, "CPE001L"),
            Curriculum("DRAW023L-1", "Computer-Aided Drafting(Lab)", 0.0, 4.5, 1.0, "DRAW021W", null),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
            Curriculum("HUM021", "Logic and Critical Thinking", 4.5, 0.0, 3.0, null, null),
        ),
        "Second Year - Term 2" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
        "Second Year - Term 3" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
    ),
    "Third Year" to listOf(
        "Third Year - Term 1" to listOf(
            Curriculum("CPE141L", "Programming Logic and Design (Lab)", 0.0, 9.0, 2.0, null, "CPE001L"),
            Curriculum("DRAW023L-1", "Computer-Aided Drafting(Lab)", 0.0, 4.5, 1.0, "DRAW021W", null),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
            Curriculum("HUM021", "Logic and Critical Thinking", 4.5, 0.0, 3.0, null, null),
        ),
        "Third Year - Term 2" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
        "Third Year - Term 3" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
    ),
    "Forth Year" to listOf(
        "Forth Year - Term 1" to listOf(
            Curriculum("CPE141L", "Programming Logic and Design (Lab)", 0.0, 9.0, 2.0, null, "CPE001L"),
            Curriculum("DRAW023L-1", "Computer-Aided Drafting(Lab)", 0.0, 4.5, 1.0, "DRAW021W", null),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
            Curriculum("HUM021", "Logic and Critical Thinking", 4.5, 0.0, 3.0, null, null),
        ),
        "Forth Year - Term 2" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
        "Forth Year - Term 3" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
            Curriculum("ENG024", "Writing for Academic Studies", 4.5, 0.0, 3.0, null, null),
            Curriculum("MATH035", "Mathematics in the Modern World", 4.5, 0.0, 3.0, null, null),
        ),
    ),
    "Professional Elective" to listOf(
        "NETA172P" to listOf(
            Curriculum("CPE141L", "Programming Logic and Design (Lab)", 0.0, 9.0, 2.0, null, "CPE001L"),
            Curriculum("DRAW023L-1", "Computer-Aided Drafting(Lab)", 0.0, 4.5, 1.0, "DRAW021W", null),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
        ),
        "MICR172P" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
        ),
        "MACH171P" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
            Curriculum("DRAW021W", "Engineering Drawing 1", 0.0, 4.5, 1.0, null, null),
        ),
        "EMSY171P" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
        ),
        "SDAD174P" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
        ),
        "AWS171P" to listOf(
            Curriculum("CPE001L", "Computer Fundamentals and Programming 1 (Lab)", 0.0, 4.5, 1.0, null, null),
            Curriculum("CPE100", "Computer Engineering as a Discipline", 1.5, 0.0, 1.0, null, null),
        ),
    ),
    "GENERAL EDUCATION ELECTIVE TITLE" to listOf(
        "Select Up to 2" to listOf(
            Curriculum("CPE141L", "Programming Logic and Design (Lab)", 0.0, 9.0, 2.0, null, "CPE001L"),
            Curriculum("DRAW023L-1", "Computer-Aided Drafting(Lab)", 0.0, 4.5, 1.0, "DRAW021W", null),
            Curriculum("ENG023", "Receptive Communication Skills", 4.5, 0.0, 3.0, null, null),
            Curriculum("HUM021", "Logic and Critical Thinking", 4.5, 0.0, 3.0, null, null),
        ),
    ),
)