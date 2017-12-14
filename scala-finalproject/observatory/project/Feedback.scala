import sbt.{Logger, Level}

import collection.mutable.ListBuffer

class GradingFeedback {

  def maxTestScore = maxScore * (1 - styleScoreRatio)

  def maxStyleScore = maxScore * styleScoreRatio

  def totalScore = vTestScore + vStyleScore

  def maxTotalScore = maxTestScore + maxStyleScore

  def feedbackString =
    s"""|${totalGradeMessage(totalScore)}
        |
        |
        |${feedbackSummary.mkString("\n\n")}
        |
        |${feedbackDetails.mkString("\n")}""".stripMargin
  def isFailed = failed

  /* Methods to build up the feedback log */

  def compileFailed(log: String) {
    failed = true
    addSummary(compileFailedMessage)
    addDetails("======== COMPILATION FAILURES ========")
    addDetails(log)
  }

  def testCompileFailed(log: String) {
    failed = true
    addSummary(testCompileFailedMessage)
    addDetails("======== TEST COMPILATION FAILURES ========")
    addDetails(log)
  }

  def allTestsPassed() {
    addSummary(allTestsPassedMessage)
    vTestScore = maxTestScore
  }

  def testsFailed(log: String, score: Double) {
    failed = true
    addSummary(testsFailedMessage(score))
    vTestScore = score
    addDetails("======== LOG OF FAILED TESTS ========")
    addDetails(log)
  }

  def testExecutionFailed(log: String) {
    failed = true
    addSummary(testExecutionFailedMessage)
    addDetails("======== ERROR LOG OF TESTING TOOL ========")
    addDetails(log)
  }

  def testExecutionDebugLog(log: String) {
    addDetails("======== DEBUG OUTPUT OF TESTING TOOL ========")
    addDetails(log)
  }

  def perfectStyle() {
    addSummary(perfectStyleMessage)
    vStyleScore = maxStyleScore
  }

  def styleProblems(log: String, score: Double) {
    addSummary(styleProblemsMessage(score))
    vStyleScore = score
    addDetails("======== CODING STYLE ISSUES ========")
    addDetails(log)
  }

  def unpackFailed(log: String) {
    failed = true
    addSummary(unpackFailedMessage)
    addDetails("======== FAILURES WHILE EXTRACTING THE SUBMISSION ========")
    addDetails(log)
  }

  def setMaxScore(newMaxScore: Double, newStyleScoreRatio: Double): Unit = {
    maxScore = newMaxScore
    styleScoreRatio = newStyleScoreRatio
  }

  private var maxScore: Double = _
  private var styleScoreRatio: Double = _

  private var vTestScore: Double = 0d
  private var vStyleScore: Double = 0d

  private val feedbackSummary = new ListBuffer[String]()
  private val feedbackDetails = new ListBuffer[String]()

  private var failed = false

  private def addSummary(msg: String): Unit =
    feedbackSummary += msg

  private def addDetails(msg: String): Unit =
    feedbackDetails += msg

  /* Feedback Messages */

  private val unpackFailedMessage =
    """Extracting the archive containing your source code failed.""".stripMargin

  private val compileFailedMessage =
    """We were not able to compile the source code you submitted. """.stripMargin

  private val testCompileFailedMessage =
    """We were not able to compile our tests, and therefore we could not correct your submission.
     .""".stripMargin

  private def testsFailedMessage(score: Double) =
    """The code you submitted did not pass all of our tests: your submission achieved a score of
      |%.2f out of %.2f in our tests.""".stripMargin.format(score, maxTestScore)

  // def so that we read the right value of vMaxTestScore (initialize modifies it)
  private def allTestsPassedMessage =
    """Your solution passed all of our tests, congratulations! You obtained the maximal test
      |score of %.2f.""".stripMargin.format(maxTestScore)

  private val testExecutionFailedMessage =
    """An error occurred while running our tests on your submission.""".stripMargin

  // def so that we read the right value of vMaxStyleScore (initialize modifies it)
  private def perfectStyleMessage =
    """style checker tool could not find any issues with your code. You obtained the maximal
      |style score of %.2f.""".stripMargin.format(maxStyleScore)

  private def styleProblemsMessage(score: Double) =
    """style checker tool found issues in your code with respect to coding style: it
      |computed a style score of %.2f out of %.2f for your submission. """.stripMargin.format(score, maxStyleScore)

  private def totalGradeMessage(score: Double) =
    """score for this assignment is %.2f out of %.2f""".format(score, maxTestScore + maxStyleScore)

}

/**
  * Logger to capture compiler output, test output
  */

object RecordingLogger extends Logger {
  private val buffer = ListBuffer[String]()

  def hasErrors = buffer.nonEmpty

  def readAndClear() = {
    val res = buffer.mkString("\n")
    buffer.clear()
    res
  }

  def clear() {
    buffer.clear()
  }

  def log(level: Level.Value, message: => String) =
    if (level == Level.Error) {
      buffer += message
    }
  def success(message: => String) = ()

  def trace(t: => Throwable) = ()
}
