import dev.kikugie.stonecutter.process.createChecksum
import dev.kikugie.stonecutter.process.toHexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

object FileProcessorTest {
    @Test
    fun `test checksums`() {
        val words: List<String> = """
            Lorem ipsum odor amet, consectetuer adipiscing elit. Inceptos diam enim primis netus vitae quam. Cursus rutrum tempor molestie netus per dapibus tempor pellentesque lacinia. Taciti habitant libero facilisis turpis efficitur viverra proin. Sapien justo libero leo justo curabitur pharetra. Lobortis nulla integer purus arcu adipiscing. Venenatis facilisis porta nascetur aptent libero platea sagittis hendrerit. Nascetur cubilia class tempor praesent; habitant ultricies donec. Eu elementum nulla egestas tempus amet at augue ligula platea. Ac cursus rhoncus elementum class laoreet amet faucibus elit nostra.
            Consectetur ridiculus ultricies enim est convallis, convallis viverra donec. Tempus maximus dis adipiscing nibh nisi sagittis blandit. Finibus mus id in at aptent natoque rutrum. Non ultrices tempus tempus; quis consequat integer. Dis sociosqu auctor varius fringilla purus integer nostra placerat. Primis condimentum interdum habitasse himenaeos potenti felis sit.
            Nostra litora sagittis viverra litora convallis lorem viverra congue lacinia. Arcu class lacinia vestibulum sollicitudin viverra in ultrices posuere. Dictumst fermentum class felis phasellus, phasellus conubia massa aenean libero. Mauris nulla aptent congue imperdiet ornare ullamcorper porta. Pharetra facilisi suscipit amet primis pulvinar egestas. Auctor dignissim fames, sed tempus neque enim porta habitasse. Euismod libero fermentum purus metus efficitur natoque fringilla in? Platea ad faucibus cras finibus nunc elit augue.
            Phasellus sodales orci ante sollicitudin pharetra ipsum litora. Vulputate quisque dictumst malesuada eleifend fusce maximus. Mus vitae etiam fringilla eget ex feugiat aptent. Porttitor ultrices quam urna quis turpis tellus. Gravida mi per aliquet habitant nec blandit himenaeos curabitur dictum. Pharetra feugiat eros himenaeos ultricies dictumst, nam egestas lacinia? Justo eros porttitor sociosqu dui dolor sociosqu erat placerat. Facilisis massa aliquam maecenas maecenas fermentum ipsum.
            Commodo quam conubia consectetur, eget bibendum dui interdum. Cubilia cursus habitant bibendum magna vestibulum natoque tincidunt. Porta proin ac per congue interdum. Ad consequat dapibus maximus imperdiet enim risus aliquet conubia. Cubilia eleifend porttitor facilisi lacus diam vestibulum platea. Nec proin enim conubia malesuada diam at donec. Aliquam eleifend odio dictum aptent, ex mus auctor amet. Urna consectetur luctus vitae fermentum morbi ullamcorper. Phasellus neque habitasse nisi malesuada semper augue vestibulum.
            Quisque duis quis pulvinar ipsum laoreet. Habitasse netus montes efficitur maecenas mollis. Mollis primis dapibus leo eleifend montes donec montes. Curae potenti tellus tortor nascetur maecenas condimentum scelerisque dis. Donec ut taciti id nunc at conubia? Ligula leo ex donec non nulla elementum. Sed finibus libero tellus montes vitae inceptos ante cras. Molestie imperdiet vel fringilla et maecenas hendrerit nulla. Pretium porta id diam dignissim facilisis sapien.
            Maecenas nostra blandit velit ex est tristique. Suspendisse rhoncus ipsum sagittis tellus vitae. Lacus nascetur ut inceptos euismod elementum phasellus sed scelerisque ultricies? Dignissim pulvinar auctor hac tincidunt, penatibus neque mauris. Vitae imperdiet porttitor ridiculus euismod himenaeos? Maecenas eros facilisis porttitor purus dictum nibh sodales magna. Placerat interdum praesent pretium nec torquent massa amet nulla. Nisl maecenas metus posuere suspendisse nostra aenean nisl natoque.
            Tortor himenaeos senectus ut sodales sodales netus eleifend etiam pretium. Condimentum mi dis vehicula pulvinar praesent interdum est. In dictum sociosqu bibendum accumsan iaculis. Vel molestie primis diam arcu ullamcorper condimentum. Ornare nascetur vel duis aliquam feugiat hac. Id blandit nostra vehicula eros natoque nullam potenti. Fames luctus venenatis bibendum ultricies ac.
            Pulvinar arcu at massa aenean conubia ullamcorper. Tempus dolor imperdiet malesuada phasellus mauris. Porttitor rhoncus auctor neque vehicula volutpat nunc sociosqu imperdiet cubilia. Litora condimentum nullam semper iaculis porttitor dui torquent? Auctor tincidunt massa porttitor dapibus egestas vehicula. Consequat urna himenaeos elit arcu habitasse dignissim! Fringilla id aliquet, pharetra felis bibendum blandit nulla quis ultricies.
            Semper netus est ullamcorper faucibus senectus nec mauris blandit. Nisi magna consectetur justo; dui libero donec. Aliquam viverra ad diam dictumst luctus urna bibendum. Habitasse dapibus ligula aenean et fames. Alacus praesent blandit litora varius ante fames. Tristique primis quisque imperdiet ultrices eleifend. Litora sit suspendisse facilisis faucibus tempor vitae tristique. Ad magna eu mollis ullamcorper fames cubilia lobortis. Penatibus bibendum risus sodales eros massa metus.
        """.trimIndent().lines().flatMap { it.split(' ') }
        val random = Random(0x147DAA91)
        val text = buildString {
            repeat(100000) { append(words.random(random)) }
        }
        val changed = buildString {
            for (it in text) if (random.nextDouble() < 0.01) append('-') else append(it)
        }
        Assertions.assertNotEquals(
            println(createChecksum(text).toHexString()),
            println(createChecksum(changed).toHexString()),
            "Checksums match (they shouldn't)"
        )
    }
}