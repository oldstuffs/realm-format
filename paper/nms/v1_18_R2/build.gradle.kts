import io.papermc.paperweight.userdev.PaperweightUser

apply<PaperweightUser>()

dependencies {
  implementation(project(":paper:nms:common"))

  paperDevBundle("1.18.2-R0.1-SNAPSHOT")
}
