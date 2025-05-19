function closePopup() {
  document.getElementById('popupModal').style.display = 'none';
}

function closePopup2() {
  document.getElementById('deleteConfirmModal').style.display = 'none';
}


let selectedBookId = null;//「どの本を削除するのか」を入れるための箱（変数）を作ってる

document.addEventListener("DOMContentLoaded", function () {//「ページが全部読み込まれたら、これをやってね！」という合図
  const deleteButtons = document.querySelectorAll(".book_delete_btn");

  deleteButtons.forEach(button => {
    button.addEventListener("click", function (event) {
      event.preventDefault(); // ページ遷移止める
      selectedBookId = button.getAttribute("data-id"); // ここで id を取る
      console.log(selectedBookId);  // ここで確認
      document.getElementById("deleteConfirmModal").style.display = "flex";
    });
  });


  document.getElementById("confirmDeleteBtn").addEventListener("click", function () {
    if (selectedBookId) {
      // ページ遷移で削除処理をする（Spring BootのControllerに任せる）
      window.location.href = `/mt_library/book/delete?id=${selectedBookId}`;
    }
  });


});


function closeModal() {
  document.getElementById("deleteConfirmModal").style.display = "none";
  selectedBookId = null;
} 