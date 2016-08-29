import java.io.StringReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.jvdev.univalidator.ConstraintsParser;
import ru.jvdev.univalidator.FieldWithConstraints;
import ru.jvdev.univalidator.exception.ConstraintsParserException;

/**
 * @author <a href="mailto:ilatypov@wiley.com">Ilshat Latypov</a>
 * @since 26.08.2016
 */
public class ConstraintsParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNullFieldnameShouldThrowException() throws Exception {
        thrown.expect(ConstraintsParserException.class);
        thrown.expectMessage(is("'fieldname' must have string value"));
        String constraints = "[{ \"fieldname\" : null }]";
        new ConstraintsParser().parse(new StringReader(constraints));
    }

    @Test
    public void testNonStringFieldnameShouldThrowException() throws Exception {
        thrown.expect(ConstraintsParserException.class);
        thrown.expectMessage(is("'fieldname' must have string value"));
        String constraints = "[{ \"fieldname\" : 15 }]";
        new ConstraintsParser().parse(new StringReader(constraints));
    }

    @Test
    public void testBlankFieldnameShouldThrowException() throws Exception {
        thrown.expect(ConstraintsParserException.class);
        thrown.expectMessage(is("'fieldname' can't be blank"));
        String constraints = "[{ \"fieldname\" : \"\" }]";
        new ConstraintsParser().parse(new StringReader(constraints));
    }

    @Test
    public void testFieldnameViolatingJavaNamingRulesShouldThrowException() throws Exception {
        thrown.expect(ConstraintsParserException.class);
        thrown.expectMessage(is("'fieldname' must be valid Java variable name"));
        String constraints = "[{ \"fieldname\" : \"wrong-java-var-name\" }]";
        new ConstraintsParser().parse(new StringReader(constraints));
    }

    @Test
    public void testProperFieldnameAndType() throws Exception {
        String constraints = "[{ \"fieldname\" : \"name\", \"type\" : \"string\" }]";
        List<FieldWithConstraints> fieldsWithConstraints = new ConstraintsParser().parse(new StringReader(constraints));
        assertEquals(1, fieldsWithConstraints.size());
    }
}
