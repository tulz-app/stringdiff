package app.tulz.diff

object RawDiffFormat extends DiffFormat[List[DiffBlock]] {

  def apply(diff: List[DiffBlock]): List[DiffBlock] = diff

}
