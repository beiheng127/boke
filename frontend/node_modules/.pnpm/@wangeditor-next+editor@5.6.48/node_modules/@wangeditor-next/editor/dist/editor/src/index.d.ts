/**
 * @description editor entry
 * @author wangfupeng
 */
import './assets/index.less';
import './utils/browser-polyfill';
import './utils/node-polyfill';
import './locale/index';
import './register-builtin-modules/index';
import './init-default-config';
import Boot from './Boot';
export { Boot };
export { createUploader, DomEditor, genModalButtonElems, genModalInputElems, genModalTextareaElems, i18nAddResources, i18nChangeLanguage, i18nGetResources, IButtonMenu, IDomEditor, IDropPanelMenu, IEditorConfig, IModalMenu, IModuleConf, ISelectMenu, IToolbarConfig, IUploadConfig, t, Toolbar, } from '@wangeditor-next/core';
export { Descendant as SlateDescendant, Editor as SlateEditor, Element as SlateElement, Location as SlateLocation, Node as SlateNode, Path as SlatePath, Point as SlatePoint, Range as SlateRange, Text as SlateText, Transforms as SlateTransforms, } from 'slate';
export { createEditor, createToolbar } from './create';
declare const _default: {};
export default _default;
