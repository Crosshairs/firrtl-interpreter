// See LICENSE for license details.

package firrtl_interpreter

import firrtl.options.Viewer.view
import firrtl.options.{DriverExecutionResult, ExecutionOptionsManager}
import firrtl.{AnnotationSeq, FirrtlSourceAnnotation, FirrtlCircuitAnnotation}
import firrtl.{FirrtlExecutionOptions, HasFirrtlExecutionOptions}
import firrtl.options.Viewer._
import firrtl.FirrtlViewer._

case class InterpreterTesterCreated(tester: InterpretiveTester) extends DriverExecutionResult
case class InterpreterTesterFailed(message: String) extends DriverExecutionResult


object Driver extends firrtl.options.Driver {

  def vcdOutputFileName(annotationSeq: AnnotationSeq): String = {
    val firrtlOptions = view[FirrtlExecutionOptions](annotationSeq).get
    s"${firrtlOptions.getBuildFileName("vcd")}"
  }

  def vcdInputFileName(annotationSeq: AnnotationSeq, fileNameOverride: Option[String] = None): String = {
    val firrtlOptions = view[FirrtlExecutionOptions](annotationSeq).get
    s"${firrtlOptions.getBuildFileName(suffix = "vcd", fileNameOverride )}"
  }

  val optionsManager: ExecutionOptionsManager = {
    new ExecutionOptionsManager("interpreter") with HasFirrtlExecutionOptions
  }

  def execute(args: Array[String], initialAnnotations: AnnotationSeq): DriverExecutionResult = {
    val annotations = optionsManager.parse(args, initialAnnotations)

    val firrtlInput = annotations.collectFirst {
      case firrtl:  InterpreterFirrtlString     => firrtl.firrtl
      case firrtl:  FirrtlSourceAnnotation  => firrtl.value
      case circuit: FirrtlCircuitAnnotation => circuit.value.serialize

    }
    val tester = new InterpretiveTester(firrtlInput.get, annotations)
    InterpreterTesterCreated(tester)
  }
}

