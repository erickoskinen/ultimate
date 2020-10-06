let _EDITOR;


/**
 * Load an add the editor template to the DOM.
 */
function load_tool_interface_template() {
  let content = $('#content');
  content.removeClass('p-5');
  const tool_interface_template = Handlebars.compile($("#tool-interface-template").html());
  content.append(tool_interface_template(_CONFIG));
}


/**
 * Initialize the frontend editor.
 */
function init_editor() {
  _EDITOR = ace.edit("editor");
  _EDITOR.renderer.setHScrollBarAlwaysVisible(false);
  _EDITOR.setTheme("ace/theme/eclipse");
  _EDITOR.getSession().setMode('ace/mode/c_cpp'); //equv to: changeMode('c_cpp');
  _EDITOR.renderer.setShowGutter(true);
  _EDITOR.setShowPrintMargin(true);
  _EDITOR.setDisplayIndentGuides(true);
  _EDITOR.setHighlightSelectedWord(true);
  _EDITOR.setPrintMarginColumn(80);

  _EDITOR.session.setValue(_CONFIG.editor.init_code);
  _EDITOR.session.setTabSize(4);
  _EDITOR.session.setUseWrapMode(true);
}


/**
 * Bind the user control buttons to process events.
 */
function init_interface_controls () {
  $('.language-selection').on({
    click: function () {
      choose_language($( this ).data().language);
      refresh_navbar();
    }
  });
  $('#navbar_execute_interface').on({
    click: function () {
      const settings = get_execute_settings();
      fetch_ultimate_results(settings);
    }
  });
}


function refresh_navbar() {
  if ("current_worker" in _CONFIG.context) {
    $('#navbar_language_select_dropdown').html('Language: ' + _CONFIG.context.current_worker.language);

    set_available_code_samples(_CONFIG.context.current_worker.language);
    set_available_frontend_settings(_CONFIG.context.current_worker.language);
    $('#navbar_execute_interface').removeClass('hidden');
  } else {
    $('#navbar_sample_select_dropdown').addClass('hidden');
    $('#navbar_execute_interface').addClass('hidden');
    $('#navbar_settings_select_dropdown').addClass('hidden');
  }
}


/**
 * Process ultimate web bridge results and add them as toasts to the editor interface.
 * @param result
 */
function add_results_to_editor(result) {
  let message;
  let messages_container = $('#messages');
  const editor_message_template = Handlebars.compile($("#editor-message").html());

  console.log(result.results);
  for (let key in result.results) {
    message = result.results[key];

    switch (message.type) {
      case "warning": {
        message.toast_classes = "border border-warning";
        break;
      }
      case "positive": {
        message.toast_classes = "border border-info";
        break;
      }
      case "invariant": {
        message.toast_classes = "border border-info";
        break;
      }
    }

    messages_container.append(editor_message_template(result.results[key]));
  }
  $('.toast').toast('show');
}


/**
 * Set (activete == true) or unset the spinner indicating the results are being fetched.
 * @param activate
 */
function set_execute_spinner(activate) {
  let exec_button = $('#navbar_execute_interface');
  if (activate) {
    exec_button.html(
      '<span class="spinner-border spinner-border-sm text-primary" role="status" aria-hidden="true"></span> Executing ...'
    );
  } else {
    exec_button.html('Execute');
  }
}


/**
 * Initiate a ultimate run and process the result.
 * @param settings
 */
function fetch_ultimate_results(settings) {
  set_execute_spinner(true);
  $.post(_CONFIG.backend.web_bridge_url, settings, function (response) {
    set_execute_spinner(false);
    add_results_to_editor(response);
  }).fail(function () {
    alert("Could not fetch results. Server error.");
  });
}


/**
 * Get the current settings Dict to be used as a new job for ultimate.
 * @returns {{user_settings: {}, code: string, action: string, toolchain: {task_id: *, id: *}}}
 */
function get_execute_settings() {
  let settings = {
    action: 'execute',
    code: _EDITOR.getSession().getValue(),
    toolchain: {
      id: _CONFIG.context.current_worker.id,
      task_id: _CONFIG.context.current_worker.task_id,
    },
    user_settings: {}
  };

  _CONFIG.context.current_worker.frontend_settings.forEach(function (setting) {
    settings.user_settings[setting.id] = $('#' + setting.id).is(':checked')
  });

  return settings;
}


/**
 * Process a language selection.
 * @param language
 */
function choose_language(language) {
  console.log('Set current language to ' + language);
  _CONFIG.context.tool.workers.forEach(function (worker) {
    if (worker.language === language) {
      _CONFIG.context.current_worker = worker;
    }
  });
}


/**
 * Set available code samples to the dropdown.
 * This is adding each example with current language match and current worker id in in the example.assoc_workers list.
 * @param language
 */
function set_available_code_samples(language) {
  let samples_menu = $('#code_sample_dropdown_menu');
  let example_entries = '';

  _CONFIG.code_examples[language].forEach(function (example) {
    if (example.assoc_workers.includes(_CONFIG.context.current_worker.id)) {
      example_entries += '<a class="dropdown-item sample-selection" href="#" data-source="' +  example.source + '">' + example.name + '</a>';
    }
  });

  if (example_entries.length > 0) {
    $('#navbar_sample_select_dropdown').removeClass('hidden');
  }
  samples_menu.html(example_entries);
  $('.sample-selection').on({
    click: function () {
      load_sample($( this ).data().source);
    }
  });
}


/**
 * Load an available sample into the editor.
 * @param source
 */
function load_sample(source) {
  $.get('config/code_examples/' + source, function (data) {
    _EDITOR.session.setValue(data);
  })
}



/**
 *
 */
function set_available_frontend_settings(language) {
  let settings_menu = $('#settings_dropdown_menu');
  let settings_entries = '';

  _CONFIG.context.current_worker.frontend_settings.forEach(function (setting) {
    if (setting.type === "bool") {
      settings_entries += '<div class="form-check">' +
        '<input type="checkbox" class="form-check-input" id="' + setting.id + '" ' + (setting.default ? "checked" : "") + '>' +
        '<label class="form-check-label" for="' + setting.id + '">' + setting.name + '</label>' +
        '</div>'
    }
  });

  if (settings_entries.length > 0) {
    $('#navbar_settings_select_dropdown').removeClass('hidden');
  }
  settings_menu.html(settings_entries);
  $('.form-check').on('click', function(e) {
    e.stopPropagation();
  });
}
