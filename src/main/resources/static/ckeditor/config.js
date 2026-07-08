CKEDITOR.editorConfig = function (config) {

   

    config.versionCheck = false;

    config.height = 700;
    config.width = "100%";

    config.enterMode = CKEDITOR.ENTER_P;
    config.shiftEnterMode = CKEDITOR.ENTER_BR;

    config.autoGrow_onStartup = true;
    config.autoGrow_minHeight = 700;
    config.autoGrow_maxHeight = 1500;

    config.removePlugins = 'resize';

    config.disableNativeSpellChecker = false;

    config.allowedContent = true;

    config.removeDialogTabs = 'image:advanced;link:advanced';


    config.font_defaultLabel = "Calibri";

    config.font_names =
        'Calibri/Calibri, sans-serif;' +
        'Arial/Arial, Helvetica, sans-serif;' +
        'Cambria/Cambria, serif;' +
        'Times New Roman/Times New Roman, Times, serif;' +
        'Georgia/Georgia, serif;' +
        'Verdana/Verdana, Geneva, sans-serif;' +
        'Tahoma/Tahoma, Geneva, sans-serif;';

    config.fontSize_defaultLabel = "12";

    config.fontSize_sizes =
        '8/8px;' +
        '9/9px;' +
        '10/10px;' +
        '11/11px;' +
        '12/12px;' +
        '14/14px;' +
        '16/16px;' +
        '18/18px;' +
        '20/20px;' +
        '24/24px;' +
        '28/28px;' +
        '32/32px;' +
        '36/36px;';



    config.contentsCss = [
        'body { font-family:Calibri; font-size:12pt; margin:20px; }'
    ];


    config.pasteFromWordRemoveFontStyles = false;
    config.pasteFromWordRemoveStyles = false;


    config.toolbar = [

        {
            name: 'document',
            items: [
                'Source',
                '-',
                'Save',
                'NewPage',
                '-',
                'Preview',
                'Print'
            ]
        },

        {
            name: 'clipboard',
            items: [
                'Cut',
                'Copy',
                'Paste',
                'PasteText',
                'PasteFromWord',
                '-',
                'Undo',
                'Redo'
            ]
        },

        {
            name: 'editing',
            items: [
                'Find',
                'Replace',
                '-',
                'SelectAll'
            ]
        },

        '/',

        {
            name: 'styles',
            items: [
                'Styles',
                'Format',
                'Font',
                'FontSize'
            ]
        },

        {
            name: 'basicstyles',
            items: [
                'Bold',
                'Italic',
                'Underline',
                'Strike',
                'Subscript',
                'Superscript',
                '-',
                'RemoveFormat'
            ]
        },

        {
            name: 'colors',
            items: [
                'TextColor',
                'BGColor'
            ]
        },

        '/',

        {
            name: 'paragraph',
            items: [
                'NumberedList',
                'BulletedList',
                '-',
                'Outdent',
                'Indent',
                '-',
                'Blockquote',
                '-',
                'JustifyLeft',
                'JustifyCenter',
                'JustifyRight',
                'JustifyBlock'
            ]
        },

        {
            name: 'insert',
            items: [
                'Link',
                'Unlink',
                '-',
                'Table',
                'HorizontalRule',
                'SpecialChar',
                'PageBreak'
            ]
        },

        {
            name: 'tools',
            items: [
                'Maximize',
                'ShowBlocks',
                '-',
                'About'
            ]
        }

    ];

};