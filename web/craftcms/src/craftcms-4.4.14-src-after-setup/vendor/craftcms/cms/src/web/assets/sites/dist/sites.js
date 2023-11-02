!function(){var t;t=jQuery,Craft.SitesAdmin=Garnish.Base.extend({$groups:null,$selectedGroup:null,init:function(){var e=this;this.$groups=t("#groups"),this.$selectedGroup=this.$groups.find("a.sel:first"),this.addListener(t("#newgroupbtn"),"activate","addNewGroup");var a=t("#groupsettingsbtn");a.length&&(a.data("menubtn").settings.onOptionSelect=function(a){var n=t(a);if(!n.hasClass("disabled"))switch(n.data("action")){case"rename":e.renameSelectedGroup();break;case"delete":e.deleteSelectedGroup()}})},addNewGroup:function(){this.promptForGroupName("").then((function(t){if(t){var e={name:t};Craft.sendActionRequest("POST","sites/save-group",{data:e}).then((function(t){location.href=Craft.getUrl("settings/sites",{groupId:t.data.group.id})})).catch((function(t){var e=t.response;e.data&&e.data.errors?Craft.cp.displayError(Craft.t("app","Could not create the group:")+"\n\n"+e.data.errors.join("\n")):Craft.cp.displayError()}))}})).catch((function(){}))},renameSelectedGroup:function(){var t=this;this.promptForGroupName(this.$selectedGroup.data("raw-name")).then((function(e){var a={id:t.$selectedGroup.data("id"),name:e};Craft.sendActionRequest("POST","sites/save-group",{data:a}).then((function(a){t.$selectedGroup.text(a.data.group.name),t.$selectedGroup.data("raw-name",e),Craft.cp.displaySuccess(Craft.t("app","Group renamed."))})).catch((function(t){var e=t.response;e.data&&e.data.errors?Craft.cp.displayError(Craft.t("app","Could not rename the group:")+"\n\n"+e.data.errors.join("\n")):Craft.cp.displayError()}))})).catch((function(){}))},promptForGroupName:function(e){return new Promise((function(a,n){Craft.sendActionRequest("POST","sites/rename-group-field",{data:{name:e}}).then((function(i){var o=t("<form/>",{class:"modal prompt"}).appendTo(Garnish.$bod),r=t("<div/>",{class:"body"}).append(i.data.html).appendTo(o),s=t("<div/>",{class:"buttons right"}).appendTo(r),d=t("<button/>",{type:"button",class:"btn",text:Craft.t("app","Cancel")}).appendTo(s);t("<button/>",{type:"submit",class:"btn submit",text:Craft.t("app","Save")}).appendTo(s),Craft.appendBodyHtml(i.data.js);var l=!1,c=new Garnish.Modal(o,{onShow:function(){setTimeout((function(){Craft.setFocusWithin(r)}),100)},onHide:function(){l||n()}});o.on("submit",(function(n){n.preventDefault();var i=t(".text",r).val();i&&i!==e&&(a(i),l=!0),c.hide()})),d.on("click",(function(){c.hide()}))}))}))},deleteSelectedGroup:function(){if(confirm(Craft.t("app","Are you sure you want to delete this group?"))){var t={id:this.$selectedGroup.data("id")};Craft.sendActionRequest("POST","sites/delete-group",{data:t}).then((function(){location.href=Craft.getUrl("settings/sites")})).catch((function(){Craft.cp.displayError()}))}},flattenErrors:function(t){var e=[];for(var a in t)t.hasOwnProperty(a)&&(e=e.concat(t[a]));return e}}),Craft.SiteAdminTable=Craft.AdminTable.extend({confirmDeleteModal:null,$rowToDelete:null,$deleteActionRadios:null,$deleteSubmitBtn:null,_deleting:!1,confirmDeleteItem:function(t){var e=this;return this.confirmDeleteModal&&(this.confirmDeleteModal.destroy(),delete this.confirmDeleteModal),this._createConfirmDeleteModal(t),Garnish.isMobileBrowser(!0)||setTimeout((function(){e.$deleteActionRadios.first().trigger("focus")}),100),!1},validateDeleteInputs:function(){var t=this.$deleteActionRadios.eq(0).prop("checked")||this.$deleteActionRadios.eq(1).prop("checked");return t?this.$deleteSubmitBtn.removeClass("disabled"):this.$deleteSubmitBtn.addClass("disabled"),t},submitDeleteSite:function(t){var e=this;if(t.preventDefault(),!this._deleting&&this.validateDeleteInputs()){this.$deleteSubmitBtn.addClass("loading"),this.disable(),this._deleting=!0;var a={id:this.getItemId(this.$rowToDelete)};this.$deleteActionRadios.eq(0).prop("checked")&&(a.transferContentTo=this.$transferSelect.val()),this.$deleteSubmitBtn.removeClass("loading"),Craft.sendActionRequest("POST",this.settings.deleteAction,{data:a}).then((function(t){e._deleting=!1,e.enable(),e.confirmDeleteModal.hide(),e.handleDeleteItemSuccess(t.data,e.$rowToDelete)}))}},_createConfirmDeleteModal:function(e){this.$rowToDelete=e;var a=this.getItemId(e),n=this.getItemName(e),i=t('<form id="confirmdeletemodal" class="modal fitted" method="post" accept-charset="UTF-8"/>').appendTo(Garnish.$bod),o=t('<div class="body"><p>'+Craft.t("app","What do you want to do with any content that is only available in {language}?",{language:n})+'</p><div class="options"><label><input type="radio" name="contentAction" value="transfer"/> '+Craft.t("app","Transfer it to:")+'</label> <div id="transferselect" class="select"><select/></div></div><div><label><input type="radio" name="contentAction" value="delete"/> '+Craft.t("app","Delete it")+"</label></div></div>").appendTo(i),r=t('<div class="buttons right"/>').appendTo(o),s=t("<button/>",{type:"button",class:"btn",text:Craft.t("app","Cancel")}).appendTo(r);this.$deleteActionRadios=o.find("input[type=radio]"),this.$transferSelect=t("#transferselect").find("> select"),this.$deleteSubmitBtn=Craft.ui.createSubmitButton({class:"disabled",label:Craft.t("app","Delete {site}",{site:n}),spinner:!0}).appendTo(r);for(var d=0;d<Craft.sites.length;d++)Craft.sites[d].id!=a&&this.$transferSelect.append('<option value="'+Craft.sites[d].id+'">'+Craft.escapeHtml(Craft.sites[d].name)+"</option>");this.confirmDeleteModal=new Garnish.Modal(i),this.addListener(s,"click",(function(){this.confirmDeleteModal.hide()})),this.addListener(this.$deleteActionRadios,"change","validateDeleteInputs"),this.addListener(i,"submit","submitDeleteSite")}})}();
//# sourceMappingURL=sites.js.map