package app.tulz.diff.compat

class IndexedSeqViewOfCharOps(
  underlying: IndexedSeqView[Char]
) {

  def mkString: String = underlying.underlying.mkString

}
