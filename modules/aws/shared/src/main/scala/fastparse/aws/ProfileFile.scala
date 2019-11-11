package fastparse.aws

final case class Profile(name: String, properties: Seq[(String, String)])

final case class ProfileFile(profiles: Seq[Profile])
