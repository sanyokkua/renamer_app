import core.utils.case_utils as cu


def test_to_camel_case():
    assert cu.to_camel_case(None) == ""
    assert cu.to_camel_case("") == ""
    assert cu.to_camel_case("    ") == ""
    assert cu.to_camel_case(666) == ""
    assert cu.to_camel_case("THIS IS CAMEL CASE") == "thisIsCamelCase"
    assert cu.to_camel_case("THIS_IS_CAMEL_CASE") == "thisIsCamelCase"
    assert cu.to_camel_case("this-IS-camel-CASE") == "thisIsCamelCase"
    assert cu.to_camel_case("THIS.IS.CAMEL.CASE") == "thisIsCamelCase"
    assert cu.to_camel_case("this_IS   .caMel-CASE") == "thisIsCamelCase"


def test_to_pascal_case():
    assert cu.to_pascal_case(None) == ""
    assert cu.to_pascal_case("") == ""
    assert cu.to_pascal_case("    ") == ""
    assert cu.to_pascal_case(666) == ""
    assert cu.to_pascal_case("THIS IS PASCAL CASE") == "ThisIsPascalCase"
    assert cu.to_pascal_case("THIS_IS_PASCAL_CASE") == "ThisIsPascalCase"
    assert cu.to_pascal_case("this-IS-pascal-casE") == "ThisIsPascalCase"
    assert cu.to_pascal_case("this.is.pascal.case") == "ThisIsPascalCase"
    assert cu.to_pascal_case("this_IS   .pascal-CASE") == "ThisIsPascalCase"


def test_to_snake_case():
    assert cu.to_snake_case(None) == ""
    assert cu.to_snake_case("") == ""
    assert cu.to_snake_case("    ") == ""
    assert cu.to_snake_case(666) == ""
    assert cu.to_snake_case("THIS IS SNAKE CASE") == "this_is_snake_case"
    assert cu.to_snake_case("THIS_IS_SNAKE_CASE") == "this_is_snake_case"
    assert cu.to_snake_case("this-IS-snake-casE") == "this_is_snake_case"
    assert cu.to_snake_case("this.is.snake.case") == "this_is_snake_case"
    assert cu.to_snake_case("this_IS   .snake-CASE") == "this_is_snake_case"


def test_to_screaming_snake_case():
    assert cu.to_screaming_snake_case(None) == ""
    assert cu.to_screaming_snake_case("") == ""
    assert cu.to_screaming_snake_case("    ") == ""
    assert cu.to_screaming_snake_case(666) == ""
    assert cu.to_screaming_snake_case("") == ""
    assert cu.to_screaming_snake_case("THIS IS SCREAMING SNAKE CASE") == "THIS_IS_SCREAMING_SNAKE_CASE"
    assert cu.to_screaming_snake_case("THIS_IS_SCREAMING_SNAKE_CASE") == "THIS_IS_SCREAMING_SNAKE_CASE"
    assert cu.to_screaming_snake_case("this-IS-sCreaming-snake-casE") == "THIS_IS_SCREAMING_SNAKE_CASE"
    assert cu.to_screaming_snake_case("this.is.screaming.snake.case") == "THIS_IS_SCREAMING_SNAKE_CASE"
    assert cu.to_screaming_snake_case("this_IS screaming  .snake-CASE") == "THIS_IS_SCREAMING_SNAKE_CASE"


def test_to_kebab_case():
    assert cu.to_kebab_case(None) == ""
    assert cu.to_kebab_case("") == ""
    assert cu.to_kebab_case("    ") == ""
    assert cu.to_kebab_case(666) == ""
    assert cu.to_kebab_case("THIS IS KEBAB CASE") == "this-is-kebab-case"
    assert cu.to_kebab_case("THIS_IS_KEBAB_CASE") == "this-is-kebab-case"
    assert cu.to_kebab_case("this-IS-kebab-casE") == "this-is-kebab-case"
    assert cu.to_kebab_case("this.is.kebab.case") == "this-is-kebab-case"
    assert cu.to_kebab_case("this_IS   .kebab-CASE") == "this-is-kebab-case"


def test_to_uppercase():
    assert cu.to_uppercase(None) == ""
    assert cu.to_uppercase("") == ""
    assert cu.to_uppercase("    ") == ""
    assert cu.to_uppercase(666) == ""
    assert cu.to_uppercase("THIS IS UPPER CASE") == "THIS IS UPPER CASE"
    assert cu.to_uppercase("THIS_IS_UPPER_CASE") == "THIS_IS_UPPER_CASE"
    assert cu.to_uppercase("this-IS-upper-casE") == "THIS-IS-UPPER-CASE"
    assert cu.to_uppercase("this.is.upper.case") == "THIS.IS.UPPER.CASE"
    assert cu.to_uppercase("this_IS   .upper-CASE") == "THIS_IS   .UPPER-CASE"


def test_to_lowercase():
    assert cu.to_lowercase(None) == ""
    assert cu.to_lowercase("") == ""
    assert cu.to_lowercase("    ") == ""
    assert cu.to_lowercase(666) == ""
    assert cu.to_lowercase("THIS IS LOWER CASE") == "this is lower case"
    assert cu.to_lowercase("THIS_IS_LOWER_CASE") == "this_is_lower_case"
    assert cu.to_lowercase("this-IS-lower-casE") == "this-is-lower-case"
    assert cu.to_lowercase("this.is.lower.case") == "this.is.lower.case"
    assert cu.to_lowercase("this_IS   .lower-CASE") == "this_is   .lower-case"


def test_to_title_case():
    assert cu.to_title_case(None) == ""
    assert cu.to_title_case("") == ""
    assert cu.to_title_case("    ") == ""
    assert cu.to_title_case(666) == ""
    assert cu.to_title_case("THIS IS TITLE CASE") == "This Is Title Case"
    assert cu.to_title_case("THIS_IS_TITLE_CASE") == "This Is Title Case"
    assert cu.to_title_case("this-IS-title-casE") == "This Is Title Case"
    assert cu.to_title_case("this.is.title.case") == "This Is Title Case"
    assert cu.to_title_case("this_IS   .title-CASE") == "This Is Title Case"
