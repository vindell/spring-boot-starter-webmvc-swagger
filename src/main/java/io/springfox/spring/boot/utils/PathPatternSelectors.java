package io.springfox.spring.boot.utils;

import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.function.Predicate;

public class PathPatternSelectors {
    private static PathPatternParser parser = new PathPatternParser();
    private PathPatternSelectors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Any path satisfies this condition
     *
     * @return predicate that is always true
     */
    public static Predicate<String> any() {
        return (each) -> true;
    }

    /**
     * No path satisfies this condition
     *
     * @return predicate that is always false
     */
    public static Predicate<String> none() {
        return (each) -> false;
    }

    /**
     * Predicate that evaluates the supplied regular expression
     *
     * @param pathRegex - regex
     * @return predicate that matches a particular regex
     */
    public static Predicate<String> regex(final String pathRegex) {
        return new Predicate<String>() {
            @Override
            public boolean test(String input) {
                return input.matches(pathRegex);
            }
        };
    }

    /**
     * Predicate that evaluates the supplied ant pattern
     *
     * @param antPattern - ant Pattern
     * @return predicate that matches a particular ant pattern
     */
    public static Predicate<String> ant(final String antPattern) {
        return new Predicate<String>() {
            @Override
            public boolean test(String input) {
                PathContainer pathContainer = PathContainer.parsePath(input, PathContainer.Options.HTTP_PATH);
                return PathPatternParser.defaultInstance.parse(antPattern).matches( pathContainer);
            }
        };
    }


}
