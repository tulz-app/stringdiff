package app.tulz.diff

sealed trait DiffBlock extends Product with Serializable

object DiffBlock {

  final case class Match(s: List[String]) extends DiffBlock
  final case class NoMatch(
    actual: List[String],
    expected: List[String]
  ) extends DiffBlock
  final case class Missing(
    expected: List[String]
  ) extends DiffBlock
  final case class Extra(
    actual: List[String]
  ) extends DiffBlock

}
